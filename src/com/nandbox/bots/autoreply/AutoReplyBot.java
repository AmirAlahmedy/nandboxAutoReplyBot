package com.nandbox.bots.autoreply;
import java.util.regex.Pattern;
import java.time.Instant;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.nandbox.bots.api.Nandbox;
import com.nandbox.bots.api.Nandbox.Api;
import com.nandbox.bots.api.NandboxClient;
import com.nandbox.bots.api.data.Chat;
import com.nandbox.bots.api.data.User;
import com.nandbox.bots.api.inmessages.BlackList;
import com.nandbox.bots.api.inmessages.ChatAdministrators;
import com.nandbox.bots.api.inmessages.ChatMember;
import com.nandbox.bots.api.inmessages.ChatMenuCallback;
import com.nandbox.bots.api.inmessages.IncomingMessage;
import com.nandbox.bots.api.inmessages.InlineMessageCallback;
import com.nandbox.bots.api.inmessages.InlineSearch;
import com.nandbox.bots.api.inmessages.MessageAck;
import com.nandbox.bots.api.inmessages.PermanentUrl;
import com.nandbox.bots.api.inmessages.WhiteList;
import com.nandbox.bots.api.outmessages.TextOutMessage;
import com.nandbox.bots.api.util.Utils;

import net.minidev.json.JSONObject;


class Helper{
	public boolean isSetMessageCommand(String messageText)
	{
		if(Pattern.compile("\\/set_message\\s.+").matcher(messageText).matches()) {
			return true;
		}
		return false;
	}
}

public class AutoReplyBot {
	
	public static String getTokenFromPropFile() throws IOException {
		Properties prop = new Properties();
	
		InputStream input = new FileInputStream("token.properties");
		prop.load(input);
		return prop.getProperty("Token");
	}
	
public static void main(String[] args) throws Exception {
		
	
		String token = getTokenFromPropFile();
		final Database db = new Database("Messages");
		NandboxClient client = NandboxClient.get();
		client.connect(token, new Nandbox.Callback() {
			Nandbox.Api api = null;
			
			@Override
			public void onConnect(Nandbox.Api api) {
			
				try {
					db.createTables();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("ONCONNECT");
				this.api = api;

			}

			@Override
			public void onReceive(IncomingMessage incomingMsg) {
				String botId = NandboxClient.getBotId();
				String chatId = incomingMsg.getChat().getId();
				Helper help = new Helper();
				System.out.println(incomingMsg.getReplyToMessageId());
				System.out.println(chatId);
				
				if(incomingMsg.getChat().getType().equalsIgnoreCase("channel"))
				{
					System.out.println(incomingMsg.getType());
					if(incomingMsg.getType().equals("text") && incomingMsg.getText().equalsIgnoreCase("/help") && incomingMsg.isFromAdmin() == 1)
					{
						TextOutMessage confirmation = new TextOutMessage();
						confirmation.setText("Hello! I am a bot that sets an auto reply message to be sent to non-admin members of the channel that will be sent to them whenever they use the chat with admin button for the first time. To set/update the message that will be sent, please type in '/set_message (Your message here)'.");
						confirmation.setChatId(chatId);
						long reference = Utils.getUniqueId();
						confirmation.setReference(reference);
						confirmation.setChatSettings(1);
						confirmation.setToUserId(incomingMsg.getFrom().getId());
						api.send(confirmation);
					}
					
					else if(incomingMsg.getType().equals("text") && help.isSetMessageCommand(incomingMsg.getText()) && incomingMsg.isFromAdmin() == 1 && incomingMsg.getChatSettings() == 1)
					{
						String parsedString[] = incomingMsg.getText().split(" ",2);
						String autoReplyMessage = parsedString[1];
						
						try {
							if(db.isSet(chatId))
							{
								db.updateMessage(chatId,autoReplyMessage);
							}
							else
							{
								db.insertMessage(chatId, autoReplyMessage);
							}
							
							TextOutMessage confirmation = new TextOutMessage();
							confirmation.setText("Auto reply message has been set");
							confirmation.setChatId(chatId);
							long reference = Utils.getUniqueId();
							confirmation.setReference(reference);
							confirmation.setChatSettings(1);
							confirmation.setToUserId(incomingMsg.getFrom().getId());
							api.send(confirmation);
							
							db.clearUsers(chatId);
							
							
						} catch (SQLException e) {
							TextOutMessage confirmation = new TextOutMessage();
							confirmation.setText("An error occured while trying to set your auto reply message.\nPlease try again later");
							confirmation.setChatId(chatId);
							long reference = Utils.getUniqueId();
							confirmation.setReference(reference);
							confirmation.setChatSettings(1);
							confirmation.setToUserId(incomingMsg.getFrom().getId());
							api.send(confirmation);
							e.printStackTrace();
						}
					} else
						try {
							System.out.println(incomingMsg.isFromAdmin());
							if(incomingMsg.getReplyToMessageId() != null /*incomingMsg.getReplyToMessageId().equals(chatId)*/ && incomingMsg.isFromAdmin() == 0 && !db.userExists(chatId, incomingMsg.getFrom().getId()))
							{
								System.out.println(incomingMsg.getReplyToMessageId());
								System.out.println(chatId+botId);
								try {
									ArrayList<String> autoReplyMessageList = db.getMessage(chatId);
									if(autoReplyMessageList.size() == 0)
									{
										TextOutMessage replyMessage = new TextOutMessage();
										replyMessage.setText("Welcome to the channel. An admin will reach out to you shortly.");
										replyMessage.setChatId(chatId);
										long reference = Utils.getUniqueId();
										replyMessage.setReference(reference);
										replyMessage.setToUserId(incomingMsg.getFrom().getId());
										replyMessage.setReplyToMessageId(chatId);
										api.send(replyMessage);
									}
									else
									{
										String replyMessageText = autoReplyMessageList.get(0);
										System.out.println(replyMessageText);
										TextOutMessage replyMessage = new TextOutMessage();
										replyMessage.setText(replyMessageText);
										replyMessage.setChatId(chatId);
										long reference = Utils.getUniqueId();
										replyMessage.setReference(reference);
										replyMessage.setToUserId(incomingMsg.getFrom().getId());
										replyMessage.setReplyToMessageId(chatId);
										api.send(replyMessage);
									}
									db.insertUser(chatId, incomingMsg.getFrom().getId());
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}

			@Override
			public void onReceive(JSONObject obj) {
			}

			@Override
			public void onClose() {
				System.out.println("ONCLOSE");
			}

			@Override
			public void onError() {
				System.out.println("ONERROR");
			}

			@Override
			public void onChatAdministrators(ChatAdministrators chatAdministrators) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatDetails(Chat chat) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatMember(ChatMember chatMember) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onChatMenuCallBack(ChatMenuCallback chatMenuCallback) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onInlineMessageCallback(InlineMessageCallback inlineMsgCallback) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onInlineSearh(InlineSearch inlineSearch) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessagAckCallback(MessageAck msgAck) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMyProfile(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUserDetails(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUserJoinedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void permanentUrl(PermanentUrl permenantUrl) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userLeftBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userStartedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void userStoppedBot(User user) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onBlackList(BlackList blackList) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onWhiteList(WhiteList whiteList) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScheduleMessage(IncomingMessage incomingScheduleMsg) {
				// TODO Auto-generated method stub
				
			}

		});

	}

}
