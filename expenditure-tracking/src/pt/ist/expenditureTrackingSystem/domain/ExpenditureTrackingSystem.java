package pt.ist.expenditureTrackingSystem.domain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jvstm.TransactionalCommand;
import pt.ist.fenixWebFramework.Config;
import pt.ist.fenixWebFramework.FenixWebFramework;
import pt.ist.fenixframework.pstm.DomainClassInfo;
import pt.ist.fenixframework.pstm.Transaction;

public class ExpenditureTrackingSystem extends ExpenditureTrackingSystem_Base {

    private static ExpenditureTrackingSystem instance = null;

    public synchronized static void initialize(final Config config) {
	if (instance == null) {
	    Transaction.withTransaction(true, new TransactionalCommand() {
		@Override
		public void doIt() {
		    // Bogus tx to load DomainClassInfo stuff...
		}
	    });
	    final long oid = readOid(config);
	    Transaction.withTransaction(false, new TransactionalCommand() {
		@Override
		public void doIt() {
		    instance = oid == -1 ? new ExpenditureTrackingSystem() : (ExpenditureTrackingSystem) Transaction
			    .getObjectForOID(oid);
		}
	    });
	}
    }

    private static long readOid(final Config config) {
	Connection connection = null;
	long oid = 0;
	try {
	    connection = FenixWebFramework.getConnection(config);

	    Statement statementLock = null;
	    ResultSet resultSetLock = null;
	    try {
		statementLock = connection.createStatement();
		resultSetLock = statementLock.executeQuery("SELECT GET_LOCK('ExpenditureTrackingSystemInit', 100)");
		if (!resultSetLock.next() || (resultSetLock.getInt(1) != 1)) {
		    throw new Error("other.app.has.lock");
		}
	    } finally {
		if (resultSetLock != null) {
		    resultSetLock.close();
		}
		if (statementLock != null) {
		    statementLock.close();
		}
	    }

	    try {
		Statement statementQuery = null;
		ResultSet resultSetQuery = null;
		try {
		    statementQuery = connection.createStatement();
		    resultSetQuery = statementQuery.executeQuery("SELECT ID_INTERNAL FROM EXPENDITURE_TRACKING_SYSTEM");
		    if (resultSetQuery.next()) {
			int idInternal = resultSetQuery.getInt(1);
			int cid = DomainClassInfo.mapClassToId(ExpenditureTrackingSystem.class);
			oid = ((long) cid << 32) + idInternal;
		    } else {
			oid = -1;
		    }
		} finally {
		    if (resultSetQuery != null) {
			resultSetQuery.close();
		    }
		    if (statementQuery != null) {
			statementQuery.close();
		    }
		}
	    } finally {
		Statement statementUnlock = null;
		try {
		    statementUnlock = connection.createStatement();
		    statementUnlock.executeUpdate("DO RELEASE_LOCK('ExpenditureTrackingSystemInit')");
		} finally {
		    if (statementUnlock != null) {
			statementUnlock.close();
		    }
		}
	    }

	    connection.commit();
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    if (connection != null) {
		try {
		    connection.close();
		} catch (SQLException e) {
		    // nothing can be done.
		}
	    }
	}
	return oid;
    }

    public static ExpenditureTrackingSystem getInstance() {
	if (instance == null) {
	    throw new Error(ExpenditureTrackingSystem.class.getName() + ".not.initialized");
	}
	return instance;
    }

    private ExpenditureTrackingSystem() {
	super();
	setAcquisitionRequestDocumentCounter(0);
    }

    public String nextAcquisitionRequestDocumentID() {
	return "D" + getAndUpdateNextAcquisitionRequestDocumentCountNumber();
    }

    public Integer nextAcquisitionRequestDocumentCountNumber() {
	return getAndUpdateNextAcquisitionRequestDocumentCountNumber();
    }

    private Integer getAndUpdateNextAcquisitionRequestDocumentCountNumber() {
	setAcquisitionRequestDocumentCounter(getAcquisitionRequestDocumentCounter().intValue() + 1);
	return getAcquisitionRequestDocumentCounter();
    }

}
