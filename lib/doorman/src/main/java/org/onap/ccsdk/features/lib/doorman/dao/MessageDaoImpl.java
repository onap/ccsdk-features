package org.onap.ccsdk.features.lib.doorman.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.onap.ccsdk.features.lib.doorman.data.ActionStatus;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageActionValue;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatusValue;
import org.onap.ccsdk.features.lib.doorman.data.Queue;
import org.onap.ccsdk.features.lib.doorman.util.JsonUtil;

public class MessageDaoImpl implements MessageDao {

    private DataSource dataSource;

    @Override
    public long addArrivedMessage(String extMessageId, MessageData request, Queue queue, Date timestamp) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                long id = 0;
                String sql = "INSERT INTO message (\n"
                        + "  ext_message_id, request_param, request_body, arrived_timestamp, queue_type, queue_id)\n"
                        + "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, extMessageId);
                    ps.setString(2, JsonUtil.dataToJson(request.getParam()));
                    ps.setString(3, request.getBody());
                    ps.setTimestamp(4, new Timestamp(timestamp.getTime()));
                    if (queue != null) {
                        ps.setString(5, queue.getType());
                        ps.setString(6, queue.getId());
                    } else {
                        ps.setNull(5, Types.VARCHAR);
                        ps.setNull(6, Types.VARCHAR);
                    }
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                con.commit();
                return id;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting message to DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMessageStarted(long messageId, Date timestamp) {
        // duplicate code with updateMessageCompleted to avoid SQL injection issue for sonar
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql = "UPDATE message SET started_timestamp = ? WHERE message_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, new Timestamp(timestamp.getTime()));
                    ps.setLong(2, messageId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating message status in DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMessageCompleted(long messageId, String resolution, Date timestamp) {
        // duplicate code with updateMessageStarted to avoid SQL injection issue for sonar
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql = "UPDATE message SET completed_timestamp = ? WHERE message_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, new Timestamp(timestamp.getTime()));
                    ps.setLong(2, messageId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating message status in DB: " + e.getMessage(), e);
        }

    }

    @Override
    public void updateMessageResponse(long messageId, Date timestamp, MessageData response) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql = "UPDATE message SET response_timestamp = ?, response_param = ?, response_body = ?\n"
                        + "WHERE message_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, new Timestamp(timestamp.getTime()));
                    ps.setString(2, JsonUtil.dataToJson(response.getParam()));
                    ps.setString(3, response.getBody());
                    ps.setLong(4, messageId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating message response in DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void addStatus(long messageId, MessageStatus status) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql = "INSERT INTO message_status (message_id, status, status_timestamp) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, messageId);
                    ps.setString(2, status.getStatus().toString());
                    ps.setTimestamp(3, new Timestamp(status.getTimestamp().getTime()));
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting message status to DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void addAction(long messageId, MessageAction action) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql = "INSERT INTO message_action (\n"
                        + "  message_id, action, action_status, resolution, action_timestamp,\n"
                        + "  done_timestamp, hold_time, response_param, response_body)\n"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setLong(1, messageId);
                    ps.setString(2, action.getAction().toString());
                    ps.setString(3, action.getActionStatus().toString());
                    ps.setString(4, action.getResolution());
                    ps.setTimestamp(5, new Timestamp(action.getTimestamp().getTime()));
                    if (action.getDoneTimestamp() != null) {
                        ps.setTimestamp(6, new Timestamp(action.getDoneTimestamp().getTime()));
                    } else {
                        ps.setNull(6, Types.TIMESTAMP);
                    }
                    ps.setInt(7, action.getHoldTime());
                    if (action.getReturnResponse() != null) {
                        ps.setString(8, JsonUtil.dataToJson(action.getReturnResponse().getParam()));
                        ps.setString(9, action.getReturnResponse().getBody());
                    } else {
                        ps.setNull(8, Types.VARCHAR);
                        ps.setNull(9, Types.VARCHAR);
                    }
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting message action to DB: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateActionDone(long actionId, Date now) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);
                String sql =
                        "UPDATE message_action SET action_status = ?, done_timestamp = ? WHERE message_action_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, ActionStatus.DONE.toString());
                    ps.setTimestamp(2, new Timestamp(now.getTime()));
                    ps.setLong(3, actionId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating action in DB: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> readMessageQueue(Queue queue) {
        List<Message> messageList = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            String msql = "SELECT * FROM message WHERE queue_type = ? AND queue_id = ?";
            String ssql = "SELECT * FROM message_status WHERE message_id = ? ORDER BY message_status_id DESC";
            String asql = "SELECT * FROM message_action WHERE message_id = ? ORDER BY message_action_id DESC";
            try (PreparedStatement mps = con.prepareStatement(msql);
                    PreparedStatement sps = con.prepareStatement(ssql);
                    PreparedStatement aps = con.prepareStatement(asql)) {
                mps.setString(1, queue.getType());
                mps.setString(2, queue.getId());
                try (ResultSet mrs = mps.executeQuery()) {
                    while (mrs.next()) {
                        long messageId = mrs.getLong("message_id");
                        String extMessageId = mrs.getString("ext_message_id");

                        Map<String, Object> requestParam =
                                (Map<String, Object>) JsonUtil.jsonToData(mrs.getString("request_param"));
                        String requestBody = mrs.getString("request_body");
                        MessageData request = null;
                        if (requestParam != null || requestBody != null) {
                            request = new MessageData(requestParam, requestBody);
                        }

                        Map<String, Object> responseParam =
                                (Map<String, Object>) JsonUtil.jsonToData(mrs.getString("response_param"));
                        String responseBody = mrs.getString("response_body");
                        MessageData response = null;
                        if (responseParam != null || responseBody != null) {
                            response = new MessageData(responseParam, responseBody);
                        }

                        Date arrivedTimestamp = mrs.getTimestamp("arrived_timestamp");
                        Date startedTimestamp = mrs.getTimestamp("started_timestamp");
                        Date completedTimestamp = mrs.getTimestamp("completed_timestamp");
                        Date responseTimestamp = mrs.getTimestamp("response_timestamp");

                        List<MessageStatus> statusHistory = new ArrayList<>();
                        sps.setLong(1, messageId);
                        try (ResultSet srs = sps.executeQuery()) {
                            while (srs.next()) {
                                MessageStatusValue status = MessageStatusValue.valueOf(srs.getString("status"));
                                Date timestamp = srs.getTimestamp("status_timestamp");
                                statusHistory.add(new MessageStatus(status, timestamp));
                            }
                        }

                        List<MessageAction> actionHistory = new ArrayList<>();
                        aps.setLong(1, messageId);
                        try (ResultSet ars = aps.executeQuery()) {
                            while (ars.next()) {
                                long actionId = ars.getLong("message_action_id");
                                MessageActionValue action = MessageActionValue.valueOf(ars.getString("action"));
                                ActionStatus actionStatus = ActionStatus.valueOf(ars.getString("action_status"));
                                String resolution = ars.getString("resolution");
                                Date timestamp = ars.getTimestamp("action_timestamp");
                                Date doneTimestamp = ars.getTimestamp("done_timestamp");
                                Integer holdTimeO = ars.getInt("hold_time");
                                int holdTime = holdTimeO != null ? holdTimeO : 0;

                                Map<String, Object> returnResponseParam =
                                        (Map<String, Object>) JsonUtil.jsonToData(ars.getString("response_param"));
                                String returnResponseBody = ars.getString("response_body");
                                MessageData returnResponse = null;
                                if (returnResponseParam != null || returnResponseBody != null) {
                                    returnResponse = new MessageData(returnResponseParam, returnResponseBody);
                                }

                                MessageAction a = new MessageAction(actionId, action, actionStatus, resolution,
                                        timestamp, doneTimestamp, holdTime, returnResponse);
                                actionHistory.add(a);
                            }
                        }

                        Message m = new Message(messageId, extMessageId, request, response, arrivedTimestamp,
                                startedTimestamp, completedTimestamp, responseTimestamp, queue, statusHistory,
                                actionHistory);
                        messageList.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading message action from DB: " + e.getMessage(), e);
        }
        return messageList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MessageAction getNextAction(long messageId) {
        try (Connection con = dataSource.getConnection()) {
            String sql = "SELECT * FROM message_action WHERE message_id = ? ORDER BY action_timestamp DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, messageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long actionId = rs.getLong("message_action_id");
                        MessageActionValue action = MessageActionValue.valueOf(rs.getString("action"));
                        ActionStatus actionStatus = ActionStatus.valueOf(rs.getString("action_status"));
                        String resolution = rs.getString("resolution");
                        Date timestamp = rs.getTimestamp("action_timestamp");
                        Date doneTimestamp = rs.getTimestamp("done_timestamp");
                        Integer holdTimeO = rs.getInt("hold_time");
                        int holdTime = holdTimeO != null ? holdTimeO : 0;

                        Map<String, Object> returnResponseParam =
                                (Map<String, Object>) JsonUtil.jsonToData(rs.getString("response_param"));
                        String returnResponseBody = rs.getString("response_body");
                        MessageData returnResponse = null;
                        if (returnResponseParam != null || returnResponseBody != null) {
                            returnResponse = new MessageData(returnResponseParam, returnResponseBody);
                        }

                        MessageAction a = new MessageAction(actionId, action, actionStatus, resolution, timestamp,
                                doneTimestamp, holdTime, returnResponse);
                        return a;
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading message action from DB: " + e.getMessage(), e);
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
