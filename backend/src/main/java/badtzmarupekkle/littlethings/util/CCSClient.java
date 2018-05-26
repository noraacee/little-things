package badtzmarupekkle.littlethings.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

public class CCSClient {
    private static final int GCM_PORT = 5235;

    private static final String ACK = "ack";
    private static final String API_KEY = "AIzaSyA844qRRRpxI82vFMaI8ip8CbEovYJgsd8";
    private static final String CONNECTION_DRAINING = "CONNECTION_DRAINING";
    private static final String CONTROL = "control";
    private static final String GCM_ELEMENT_NAME = "gcm";
    private static final String GCM_NAMESPACE = "google:mobile:data";
    private static final String GCM_SENDER_ADDRESS = "@gcm.googleapis.com";
    private static final String GCM_SERVER = "gcm.googleapis.com";
    private static final String MESSAGE_ID_HEADER = "m-";
    private static final String NACK = "nack";
    private static final String PROPERTY_COLLAPSE_KEY = "collapse_key";
    private static final String PROPERTY_CONTROL_TYPE = "control_type";
    private static final String PROPERTY_DATA = "data";
    private static final String PROPERTY_DELAY_WHILE_IDLE = "delay_while_idle";
    private static final String PROPERTY_FROM = "from";
    private static final String PROPERTY_MESSAGE_ID = "message_id";
    private static final String PROPERTY_MESSAGE_TYPE = "message_type";
    private static final String PROPERTY_TIME_TO_LIVE = "time_to_live";
    private static final String PROPERTY_TO = "to";
    private static final String SENDER_ID = "353726664694";

    static {
        ProviderManager.addExtensionProvider(GCM_ELEMENT_NAME, GCM_NAMESPACE, new PacketExtensionProvider() {
            @Override
            public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
                String json = parser.nextText();
                return new GCMPacketExtension(json);
            }
        });
    }

    private XMPPConnection connection;
    protected volatile boolean connectionDraining = false;

    public CCSClient() throws XMPPException, IOException, SmackException {
        connect();
    }

    public static String createJSONMessage(String to, Map<String, String> payload, String collapseKey, long timeToLive, boolean delayWhileIdle) {
        Map<String, Object> message = new HashMap<>();
        message.put(PROPERTY_TO, to);
        message.put(PROPERTY_MESSAGE_ID, nextMessageId());

        if (collapseKey != null)
            message.put(PROPERTY_COLLAPSE_KEY, collapseKey);
        if (delayWhileIdle)
            message.put(PROPERTY_DELAY_WHILE_IDLE, true);
        if (payload != null)
            message.put(PROPERTY_DATA, payload);
        if (timeToLive != -1)
            message.put(PROPERTY_TIME_TO_LIVE, timeToLive);

        return JSONValue.toJSONString(message);
    }

    public boolean sendDownstreamMessage(String jsonRequest) throws NotConnectedException {
        if (!connectionDraining) {
            send(jsonRequest);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void connect() throws XMPPException, IOException, SmackException {
        ConnectionConfiguration config = new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
        config.setSecurityMode(SecurityMode.enabled);
        config.setReconnectionAllowed(true);
        config.setRosterLoadedAtLogin(false);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());

        connection = new XMPPTCPConnection(config);
        connection.connect();

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message incomingMessage = (Message) packet;
                GCMPacketExtension gcmPacket = (GCMPacketExtension) incomingMessage.getExtension(GCM_NAMESPACE);
                String json = gcmPacket.getJson();
                try {
                    Map<String, Object> jsonObject = (Map<String, Object>) JSONValue.parseWithException(json);
                    String messageType = (String) jsonObject.get(PROPERTY_MESSAGE_TYPE);

                    if (messageType == null) {
                        handleUpstreamMessage(jsonObject);
                        String from = (String) jsonObject.get(PROPERTY_FROM);
                        String messageId = (String) jsonObject.get(PROPERTY_MESSAGE_ID);
                        String ack = createJSONAck(from, messageId);
                        send(ack);
                    } else if (messageType.equals(ACK)) {
                        handleAckReceipt(jsonObject);
                    } else if (messageType.equals(NACK)) {
                        handleNackReceipt(jsonObject);
                    } else if (messageType.equals(CONTROL)) {
                        handleControlMessage(jsonObject);
                    }
                } catch (ParseException e) {
                } catch (Exception e) {
                }
            }
        }, new PacketTypeFilter(Message.class));

        connection.login(SENDER_ID + GCM_SENDER_ADDRESS, API_KEY);
    }

    private String createJSONAck(String to, String messageId) {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put(PROPERTY_MESSAGE_ID, messageId);
        message.put(PROPERTY_MESSAGE_TYPE, ACK);
        message.put(PROPERTY_TO, to);
        return JSONValue.toJSONString(message);
    }

    private void handleAckReceipt(Map<String, Object> jsonObject) {
    }

    private void handleControlMessage(Map<String, Object> jsonObject) {
        String controlType = (String) jsonObject.get(PROPERTY_CONTROL_TYPE);
        if (controlType.equals(CONNECTION_DRAINING))
            connectionDraining = true;
    }

    private void handleNackReceipt(Map<String, Object> jsonObject) {
    }

    private void handleUpstreamMessage(Map<String, Object> jsonObject) {
    }

    private static String nextMessageId() {
        return MESSAGE_ID_HEADER + UUID.randomUUID().toString();
    }

    private void send(String jsonRequest) throws NotConnectedException {
        Packet request = new GCMPacketExtension(jsonRequest).toPacket();
        connection.sendPacket(request);
    }

    private static final class GCMPacketExtension extends DefaultPacketExtension {

        private final String json;

        public GCMPacketExtension(String json) {
            super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
            this.json = json;
        }

        public String getJson() {
            return json;
        }

        @Override
        public String toXML() {
            return String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME, GCM_NAMESPACE, StringUtils.escapeForXML(json), GCM_ELEMENT_NAME);
        }

        public Packet toPacket() {
            Message message = new Message();
            message.addExtension(this);
            return message;
        }
    }
}
