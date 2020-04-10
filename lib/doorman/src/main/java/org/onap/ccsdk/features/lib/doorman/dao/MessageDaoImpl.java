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
				String sql = "INSERT INTO message (ext_message_id, request_param, request_body, arrived_timestamp, queue_type, queue_id) VALUES (?, ?, ?, ?, ?, ?)";
				try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
					ps.setString(1, extMessageId);
					ps.setString(2, JsonUtil.dataToJson(request.param));
					ps.setString(3, request.body);
					ps.setTimestamp(4, new Timestamp(timestamp.getTime()));
					if (queue != null) {
						ps.setString(5, queue.type);
						ps.setString(6, queue.id);
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
		updateMessageStatus("started_timestamp", messageId, null, timestamp);
	}

	@Override
	public void updateMessageCompleted(long messageId, String resolution, Date timestamp) {
		updateMessageStatus("completed_timestamp", messageId, resolution, timestamp);
	}

	private void updateMessageStatus(String timestampColumn, long messageId, String resolution, Date timestamp) {
		try (Connection con = dataSource.getConnection()) {
			try {
				con.setAutoCommit(false);
				String sql = "UPDATE message SET " + timestampColumn + " = ? WHERE message_id = ?";
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
				String sql = "UPDATE message SET response_timestamp = ?, response_param = ?, response_body = ? WHERE message_id = ?";
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setTimestamp(1, new Timestamp(timestamp.getTime()));
					ps.setString(2, JsonUtil.dataToJson(response.param));
					ps.setString(3, response.body);
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
					ps.setString(2, status.status.toString());
					ps.setTimestamp(3, new Timestamp(status.timestamp.getTime()));
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
				String sql = "INSERT INTO message_action (message_id, action, action_status, resolution, action_timestamp, done_timestamp, hold_time, response_param, response_body) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement ps = con.prepareStatement(sql)) {
					ps.setLong(1, messageId);
					ps.setString(2, action.action.toString());
					ps.setString(3, action.actionStatus.toString());
					ps.setString(4, action.resolution);
					ps.setTimestamp(5, new Timestamp(action.timestamp.getTime()));
					if (action.doneTimestamp != null) {
						ps.setTimestamp(6, new Timestamp(action.doneTimestamp.getTime()));
					} else {
						ps.setNull(6, Types.TIMESTAMP);
					}
					ps.setInt(7, action.holdTime);
					if (action.returnResponse != null) {
						ps.setString(8, JsonUtil.dataToJson(action.returnResponse.param));
						ps.setString(9, action.returnResponse.body);
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
				String sql = "UPDATE message_action SET action_status = ?, done_timestamp = ? WHERE message_action_id = ?";
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
			try (PreparedStatement mps = con.prepareStatement(msql); PreparedStatement sps = con.prepareStatement(ssql); PreparedStatement aps = con.prepareStatement(asql)) {
				mps.setString(1, queue.type);
				mps.setString(2, queue.id);
				try (ResultSet mrs = mps.executeQuery()) {
					while (mrs.next()) {
						Message m = new Message();
						m.messageId = mrs.getLong("message_id");
						m.extMessageId = mrs.getString("ext_message_id");
						m.request = new MessageData();
						m.request.param = (Map<String, Object>) JsonUtil.jsonToData(mrs.getString("request_param"));
						m.request.body = mrs.getString("request_body");
						m.response = new MessageData();
						m.response.param = (Map<String, Object>) JsonUtil.jsonToData(mrs.getString("response_param"));
						m.response.body = mrs.getString("response_body");
						m.queue = new Queue();
						m.queue.type = mrs.getString("queue_type");
						m.queue.id = mrs.getString("queue_id");
						m.arrivedTimestamp = mrs.getTimestamp("arrived_timestamp");
						m.startedTimestamp = mrs.getTimestamp("started_timestamp");
						m.completedTimestamp = mrs.getTimestamp("completed_timestamp");
						m.responseTimestamp = mrs.getTimestamp("response_timestamp");
						m.statusHistory = new ArrayList<>();
						m.actionHistory = new ArrayList<>();
						messageList.add(m);

						sps.setLong(1, m.messageId);
						try (ResultSet srs = sps.executeQuery()) {
							while (srs.next()) {
								MessageStatus s = new MessageStatus();
								s.status = MessageStatusValue.valueOf(srs.getString("status"));
								s.timestamp = srs.getTimestamp("status_timestamp");
								m.statusHistory.add(s);
							}
						}

						aps.setLong(1, m.messageId);
						try (ResultSet ars = aps.executeQuery()) {
							while (ars.next()) {
								MessageAction a = new MessageAction();
								a.actionId = ars.getLong("message_action_id");
								a.action = MessageActionValue.valueOf(ars.getString("action"));
								a.actionStatus = ActionStatus.valueOf(ars.getString("action_status"));
								a.timestamp = ars.getTimestamp("action_timestamp");
								a.doneTimestamp = ars.getTimestamp("done_timestamp");
								a.holdTime = ars.getInt("hold_time");
								a.returnResponse = new MessageData();
								a.returnResponse.param = (Map<String, Object>) JsonUtil.jsonToData(ars.getString("response_param"));
								a.returnResponse.body = ars.getString("response_body");
								if (a.returnResponse.param == null && a.returnResponse.body == null) {
									a.returnResponse = null;
								}
								a.resolution = ars.getString("resolution");
								m.actionHistory.add(a);
							}
						}
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
						MessageAction a = new MessageAction();
						a.actionId = rs.getLong("message_action_id");
						a.action = MessageActionValue.valueOf(rs.getString("action"));
						a.actionStatus = ActionStatus.valueOf(rs.getString("action_status"));
						a.timestamp = rs.getTimestamp("action_timestamp");
						a.doneTimestamp = rs.getTimestamp("done_timestamp");
						a.holdTime = rs.getInt("hold_time");
						a.returnResponse = new MessageData();
						a.returnResponse.param = (Map<String, Object>) JsonUtil.jsonToData(rs.getString("response_param"));
						a.returnResponse.body = rs.getString("response_body");
						if (a.returnResponse.param == null && a.returnResponse.body == null) {
							a.returnResponse = null;
						}
						a.resolution = rs.getString("resolution");
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
