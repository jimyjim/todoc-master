package com.cleanup.todoc;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cleanup.todoc.database.TodocDatabase;
import com.cleanup.todoc.model.Project;
import com.cleanup.todoc.model.Task;
import com.cleanup.todoc.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ItemDaoTest {

    // FOR DATA
    private TodocDatabase database;
    private static long PROJECT_ID = 1;
    private static Project PROJECT_DEMO = new Project(PROJECT_ID, "Projet Tartampion", 0xFFEADAD1);
    private static Task NEW_TASK_1 = new Task(1, PROJECT_ID, "tache1", new Date().getTime());
    private static Task NEW_TASK_2 = new Task(2, PROJECT_ID, "tache2", new Date().getTime());
    private static Task NEW_TASK_3 = new Task(3, PROJECT_ID, "tache3", new Date().getTime());
    private static Project[] project;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() throws Exception {
        this.database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                TodocDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }
    @Test
    public void insertAndGetProject() throws InterruptedException {
        // BEFORE : Adding a new project
        this.database.projectDao().createProject(PROJECT_DEMO);
        // TEST
        Project project = LiveDataTestUtil.getValue(this.database.projectDao().getProject(PROJECT_ID));
        assertTrue(project.getName().equals(PROJECT_DEMO.getName()) && project.getId() == PROJECT_ID);
    }
    @Test
    public void getItemsWhenNoTaskInserted() throws InterruptedException {
        // TEST
        List<Task> task = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID));
        assertTrue(task.isEmpty());
    }
    @Test
    public void insertAndGetItems() throws InterruptedException {

        this.database.projectDao().createProject(PROJECT_DEMO);
        this.database.taskDao().insertTask(NEW_TASK_1);
        this.database.taskDao().insertTask(NEW_TASK_2);
        this.database.taskDao().insertTask(NEW_TASK_3);


        // TEST
        List<Task> task = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID));
        assertTrue(task.size() == 3);
    }

    @Test
    public void insertAndUpdateItem() throws InterruptedException {
        this.database.projectDao().createProject(PROJECT_DEMO);
        this.database.taskDao().insertTask(NEW_TASK_1);
        Task taskAdded = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID)).get(0);
        taskAdded.setName("tache4");
        this.database.taskDao().updateTask(taskAdded);

        //TEST
        List<Task> task = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID));
        assertTrue(task.size() == 1 && task.get(0).getName().equals("tache4"));
    }

    @Test
    public void insertAndDeleteItem() throws InterruptedException {
        this.database.projectDao().createProject(PROJECT_DEMO);
        this.database.taskDao().insertTask(NEW_TASK_1);
        Task taskAdded = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID)).get(0);
        this.database.taskDao().deleteItem(taskAdded.getId());

        //TEST
        List<Task> task = LiveDataTestUtil.getValue(this.database.taskDao().getItems(PROJECT_ID));
        assertTrue(task.isEmpty());
    }
}