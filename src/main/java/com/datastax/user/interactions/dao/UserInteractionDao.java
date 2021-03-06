package com.datastax.user.interactions.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.user.interactions.model.UserInteraction;

public class UserInteractionDao {

	private static Logger logger = LoggerFactory.getLogger(UserInteractionDao.class);
	private Session session;

	private DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
	private static String keyspaceName = "datastax_user_interactions_demo";
	private static String userInteractionsTable = keyspaceName + ".user_interactions";
	private static String counterTable = keyspaceName + ".user_interaction_counters";

	private static final String INSERT_INTO_USER_INTERACTION = "Insert into " + userInteractionsTable
			+ " (user_id, app, time, action) values (?,?,?,?);";

	private static final String UPDATE_COUNTER = "update " + counterTable + " "
			+ " set count=count+1  where user_id = ? and app = ? and action=?;";

	private PreparedStatement insertUserInteractionStmt;
	private PreparedStatement updateCounter;

	public UserInteractionDao(String[] contactPoints) {

		Cluster cluster = Cluster.builder().addContactPoints(contactPoints).build();

		this.session = cluster.connect();

		this.insertUserInteractionStmt = session.prepare(INSERT_INTO_USER_INTERACTION);
		this.updateCounter = session.prepare(UPDATE_COUNTER);

		this.insertUserInteractionStmt.setConsistencyLevel(ConsistencyLevel.ALL);
		this.updateCounter.setConsistencyLevel(ConsistencyLevel.ALL);
	}

	public void insertUserInteraction(List<UserInteraction> userInteractions) {

		for (UserInteraction userInteraction : userInteractions) {
			session.execute(this.insertUserInteractionStmt.bind(userInteraction.getUserId(), userInteraction.getApp(),
					userInteraction.getTime(), userInteraction.getAction()));

			session.execute(this.updateCounter.bind(userInteraction.getUserId(), userInteraction.getApp(),
					userInteraction.getAction()));
		}
	}
}
