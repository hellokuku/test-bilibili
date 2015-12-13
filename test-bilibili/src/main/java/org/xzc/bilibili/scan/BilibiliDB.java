package org.xzc.bilibili.scan;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.task.CommentTask;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

@Component
public class BilibiliDB {
	@Autowired
	private ConnectionSource cs;
	RuntimeExceptionDao<Video, Integer> videoDao;
	RuntimeExceptionDao<CommentTask, Integer> commentTaskDao;

	@PostConstruct
	public void init() throws SQLException {
		TableUtils.createTableIfNotExists( cs, Video.class );
		TableUtils.createTableIfNotExists( cs, CommentTask.class );
		videoDao = new RuntimeExceptionDao( DaoManager.createDao( cs, Video.class ) );
		commentTaskDao = new RuntimeExceptionDao( DaoManager.createDao( cs, CommentTask.class ) );
	}

	public void addOrUpdateCommentTask(CommentTask ct) {
		commentTaskDao.createOrUpdate( ct );
	}

	public List<CommentTask> getCommentTaskList() {
		return commentTaskDao.queryForEq( "status", 0 );
	}

	public void markFinished(CommentTask ct) {
		ct.status = 1;
		commentTaskDao.update( ct );
	}

	public void add(Video v) {
		videoDao.create( v );
	}

	public Video getVideo(int aid) {
		return videoDao.queryForId( aid );
	}

	public List<Video> getMQXList(int maxResults) {
		try {
			QueryBuilder<Video, Integer> qb = videoDao.queryBuilder();
			Where<Video, Integer> w = qb.where();
			w.and( w.eq( "state", 1 ), w.or( w.eq( "title", "" ), w.isNull( "title" ) ) );
			qb.limit( maxResults );
			//qb.where().eq( "state", 1 ).and().eq( "title", "" );
			return qb.query();
		} catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}

	public void createOrUpdate(Video v) {
		v.updateAt = new Date();
		videoDao.createOrUpdate( v );
	}

	public void update(Video v) {
		v.updateAt = new Date();
		videoDao.update( v );
	}

	public int getMaxAid(int defaultValue) {
		//获得最大的aix
		try {
			String str = videoDao.queryBuilder().selectRaw( "max(aid)" ).queryRawFirst()[0];
			return str == null ? defaultValue : Integer.parseInt( str );
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	public List<Video> getVideoByStateAndTypeID(int state, int typeid, int maxResults) {
		try {
			return videoDao.queryBuilder().limit( maxResults ).where().eq( "state", state ).and().eq( "typeid", typeid )
					.query();
		} catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * page基于0
	 * @param state
	 * @param pagesize
	 * @param page
	 * @return
	 */
	public Page<Video> getVideoByState(int state, int pagesize, int page) {
		try {
			List<Video> list = videoDao.queryBuilder().offset( page * pagesize ).limit( pagesize ).where()
					.eq( "state", state ).query();
			Page<Video> p = new Page<Video>();
			p.list = list;
			p.page = page;
			p.pagesize = pagesize;
			p.total = (int) videoDao.queryBuilder().where().eq( "state", state ).countOf();
			return p;
		} catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}

	public List<Video> getVideoByState(int state) {
		try {
			return videoDao.queryBuilder().where().eq( "state", state ).query();
		} catch (SQLException e) {
			throw new RuntimeException( e );
		}
	}

	public RuntimeExceptionDao<Video, Integer> getVideoDao() {
		return videoDao;
	}

	public void fixCommentTask() {
	}

	public void markFailed(CommentTask ct) {
		ct.status = 3;
		commentTaskDao.update( ct );
	}
	
}
