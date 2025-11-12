package jp.ats.backsight;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jp.ats.substrate.util.SoftReferenceCache;

@WebListener
public class ThreadCleaner implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		SoftReferenceCache.stopRemoval();
	}
}
