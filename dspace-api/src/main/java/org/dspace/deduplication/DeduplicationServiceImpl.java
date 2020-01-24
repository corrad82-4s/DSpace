/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.deduplication;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.deduplication.dao.DeduplicationDAO;
import org.dspace.deduplication.service.DeduplicationService;
import org.springframework.beans.factory.annotation.Autowired;

public class DeduplicationServiceImpl implements DeduplicationService {

	/**
	 * log4j logger
	 */
	private final Logger log = org.apache.logging.log4j.LogManager.getLogger(DeduplicationServiceImpl.class);

	@Autowired(required = true)
	protected DeduplicationDAO deduplicationDAO;

	protected DeduplicationServiceImpl() {
		super();
	}

	@Override
	public Deduplication create(Context context) throws SQLException {
		// Create a table row
		Deduplication dedup = deduplicationDAO.create(context, new Deduplication());

		dedup.setDeduplicationId(deduplicationDAO.getNextDeduplicationId(context));

		return dedup;
	}

	@Override
	public List<Deduplication> findAll(Context context) throws SQLException {
		return deduplicationDAO.findAll(context);
	}

	@Override
	public int countTotal(Context context) throws SQLException {
		return deduplicationDAO.countRows(context);
	}

	@Override
	public void update(Context context, Deduplication dedup) throws SQLException {
		deduplicationDAO.save(context, dedup);
	}

	@Override
	public List<Deduplication> getDeduplicationByFirstAndSecond(Context context, String firstId, String secondId)
			throws SQLException {
		return deduplicationDAO.findByFirstAndSecond(context, firstId, secondId);
	}

	@Override
	public Deduplication uniqueDeduplicationByFirstAndSecond(Context context, String firstId, String secondId)
			throws SQLException {
		return deduplicationDAO.uniqueByFirstAndSecond(context, firstId, secondId);
	}
}
