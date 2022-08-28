package javagrinko.spring.tcp;


import com.icedberries.UBFunkeysServer.domain.User;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface Server {
    int getConnectionsCount();
    void setPort(Integer port);
    void start();
    void stop();
    List<Connection> getConnections();
    void addListener(Connection.Listener listener);
    HashMap<UUID, User> getConnectedUsers();
    void addConnectedUser(UUID uuid, User user);
    void removeConnectedUser(UUID uuid);
}
