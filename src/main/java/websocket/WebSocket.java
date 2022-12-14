package websocket;



import com.google.gson.Gson;
import model.Message;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/chat/{client_id}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)


public class WebSocket {



    private Session session;

    private String room;
    private static final Set<WebSocket> chatEndpoints = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();



    @OnOpen
    public void onOpen(Session session, @PathParam("client_id") String username) throws EncodeException, IOException {
        System.out.println("user name:" +username);
        System.out.println("opening websocket");
        this.room = username;
        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), username);

    }

    @OnClose
    public void onClose(Session session){
        chatEndpoints.remove(this);
    }


    @OnMessage
    public String handleTextMessage(String message) throws EncodeException, IOException {

            Message messageObj = new Message();

            if(room.equalsIgnoreCase("all")){

                Gson gson = new Gson();
                Message jsonObjects = gson.fromJson(message,Message.class);
                messageObj.setWhatsapp(jsonObjects.getWhatsapp());
                messageObj.setTelegram(jsonObjects.getTelegram());
                messageObj.setInstagram(jsonObjects.getInstagram());
                messageObj.setFacebook(jsonObjects.getFacebook());
                broadcast(messageObj,room);

            }
            else{

                Gson gson = new Gson();
                Message jsonObjects = gson.fromJson(message,Message.class);
                messageObj.setFrom(jsonObjects.getFrom());
                messageObj.setContent(jsonObjects.getContent());
                broadcast(messageObj,room);

            }

            return message;


        }




    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

/*
    @OnMessage(maxMessageSize = 1024000)
    public byte handleBinaryMessage(byte buffer) {
        System.out.println("New Binary Message Received");
        return buffer;
    }
*/

    private static void broadcast(Message message,String room) throws IOException, EncodeException {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {

                    if(room.equalsIgnoreCase("all")){
                        endpoint.session.getBasicRemote()
                                .sendObject(message);
                    }



                    if(users.get(endpoint.session.getId()).equalsIgnoreCase(room)){
                        endpoint.session.getBasicRemote()
                                .sendObject(message);

                    }



                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private static void broadcastQueue(Message message,String room){
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {


                        endpoint.session.getBasicRemote()
                                .sendObject(message);



                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }






}