package jp.gr.java_conf.shioyang.mytasks2;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncLoadTasks extends AsyncTask<Void, Void, Boolean> {
    final MainActivity activity;
    final Tasks client;
    private final ProgressBar progressBar;

    public AsyncLoadTasks(MainActivity tasksActivity) {
        super();
        this.activity = tasksActivity;
        client = tasksActivity.service;
        progressBar = (ProgressBar) tasksActivity.findViewById(R.id.progressBar);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        List<String> result = new ArrayList<>();
        try {
            List<Task> tasks = client.tasks().list("@default").setFields("items/title").execute().getItems();
            if (tasks != null) {
                for (Task task : tasks) {
                    result.add(task.getTitle());
                }
            } else {
                result.add("No tasks...");
            }
            activity.tasksList = result;
            return true;
        } catch (UserRecoverableAuthIOException userRecoverableAuthIOException) {
            activity.startActivityForResult(userRecoverableAuthIOException.getIntent(), activity.REQUEST_AUTHORIZATION);
            // The result is handled in MainActivity.onActivityResult().
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // fail
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        progressBar.setVisibility(View.GONE);
        if (isSuccess)
            activity.refreshView();
    }

    static void run(MainActivity tasksActivity) {
        new AsyncLoadTasks(tasksActivity).execute();
    }
}
