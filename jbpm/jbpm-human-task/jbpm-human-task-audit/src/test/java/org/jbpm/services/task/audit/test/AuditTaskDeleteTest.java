/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.task.audit.test;

import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;
import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.strategy.StandaloneJtaStrategy;
import org.jbpm.process.instance.impl.util.LoggingPrintStream;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.jbpm.services.task.audit.service.TaskJPAAuditService;
import org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener;
import org.jbpm.services.task.utils.TaskFluent;
import org.junit.After;
//import org.jbpm.process.instance.impl.util.LoggingPrintStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.audit.query.AuditTaskInstanceLogDeleteBuilder;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.InternalTaskService;

public class AuditTaskDeleteTest extends TaskJPAAuditService {
    
    private static HashMap<String, Object> context;
    private static EntityManagerFactory emf;

    private Task [] taskTestData;
    
    
    @BeforeClass
    public static void configure() { 
        LoggingPrintStream.interceptSysOutSysErr();
        
        
    }
    
    @AfterClass
    public static void reset() { 
        LoggingPrintStream.resetInterceptSysOutSysErr();
        
    }

    @Before
    public void setUp() throws Exception {
    	context = setupWithPoolingDataSource("org.jbpm.services.task", "jdbc/jbpm-ds");
        emf = (EntityManagerFactory) context.get(ENTITY_MANAGER_FACTORY);
        this.persistenceStrategy = new StandaloneJtaStrategy(emf);
        
        produceTaskInstances();
    }
    
    @After
    public void cleanup() {
    	cleanUp(context);
    }
   
    private static Random random = new Random();

    private Calendar randomCal() { 
        Calendar cal = GregorianCalendar.getInstance();
        cal.roll(Calendar.DAY_OF_YEAR, -1*random.nextInt(10*365));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }
    
    private void produceTaskInstances() {
    	InternalTaskService taskService = (InternalTaskService) HumanTaskServiceFactory.newTaskServiceConfigurator()
				.entityManagerFactory(emf)
				.listener(new JPATaskLifeCycleEventListener(true))
				.listener(new BAMTaskEventListener(true))
				.getTaskService();
    	
    	Calendar cal = randomCal();
    	String processId = "process";
    	taskTestData = new Task[10];
    	
    	for (int i = 0; i < 10; i++) {
    		cal.add(Calendar.HOUR_OF_DAY, 1);
    		Task task = new TaskFluent().setName("This is my task name")
                 .addPotentialGroup("Knights Templer")
                 .setAdminUser("Administrator")
                 .setProcessId(processId + i)
                 .setCreatedOn(cal.getTime())
                 .getTask();

			taskService.addTask(task, new HashMap<String, Object>());	
			taskTestData[i] = task;
    	}
    }

    @Test
    public void testDeleteAuditTaskInfoLogByProcessId() { 
        int p = 0;
        String processId = taskTestData[p++].getTaskData().getProcessId();
        String processId2 = taskTestData[p++].getTaskData().getProcessId();
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().processId(processId, processId2);
        int result = updateBuilder.build().execute();
        assertEquals(2, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByDate() { 
        int p = 0;        
        Date endDate = taskTestData[p++].getTaskData().getCreatedOn();
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().date(endDate);
        int result = updateBuilder.build().execute();
        assertEquals(1, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByProcessIdAndDate() { 
        int p = 0;     
        String processId = taskTestData[p].getTaskData().getProcessId();
        Date endDate = taskTestData[p].getTaskData().getCreatedOn();
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().date(endDate).processId(processId);
        int result = updateBuilder.build().execute();
        assertEquals(1, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByProcessIdAndNotMatchingDate() { 
        int p = 0;     
        String processId = taskTestData[p++].getTaskData().getProcessId();
        Date endDate = taskTestData[p++].getTaskData().getCreatedOn();
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().date(endDate).processId(processId);
        int result = updateBuilder.build().execute();
        assertEquals(0, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByDateRangeEnd() { 
        
        Date endDate = taskTestData[4].getTaskData().getCreatedOn();
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().dateRangeEnd(endDate);
        int result = updateBuilder.build().execute();
        assertEquals(5, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByDateRangeStart() { 
        
        Date endDate = taskTestData[8].getTaskData().getCreatedOn();
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().dateRangeStart(endDate);
        int result = updateBuilder.build().execute();
        assertEquals(2, result);
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByDateRange() { 
        
    	Date startDate = taskTestData[4].getTaskData().getCreatedOn();
        Date endDate = taskTestData[8].getTaskData().getCreatedOn();
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().dateRangeStart(startDate).dateRangeEnd(endDate);
        int result = updateBuilder.build().execute();
        assertEquals(5, result);
    }
    
    @Test
    public void testTaskAuditServiceClear() { 
               
        List<AuditTask> data = this.auditTaskInstanceLogQuery().buildQuery().getResultList(); 
        assertEquals(10, data.size());
        
        this.clear();
        
        data = this.auditTaskInstanceLogQuery().buildQuery().getResultList();       
        assertEquals(0, data.size());
    }
    
    @Test
    public void testDeleteAuditTaskInfoLogByTimestamp() { 

        List<AuditTask> tasks = this.auditTaskInstanceLogQuery().taskId(taskTestData[4].getId()).buildQuery().getResultList();
        assertEquals(1, tasks.size());
        
        AuditTaskInstanceLogDeleteBuilder updateBuilder = this.auditTaskInstanceLogDelete().date(tasks.get(0).getCreatedOn());
        int result = updateBuilder.build().execute();
        assertEquals(1, result);
    }
      
}