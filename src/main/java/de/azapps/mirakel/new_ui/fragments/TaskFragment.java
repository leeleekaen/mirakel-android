/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 *   Copyright (c) 2013-2015 Anatolij Zelenin, Georg Semmler.
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package de.azapps.mirakel.new_ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.calendar.recurrencepicker.RecurrencePickerDialog;
import com.fourmob.datetimepicker.date.DatePicker;
import com.fourmob.datetimepicker.date.SupportDatePickerDialog;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.azapps.material_elements.utils.SoftKeyboard;
import de.azapps.material_elements.utils.ThemeManager;
import de.azapps.mirakel.helper.AnalyticsWrapperBase;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.MirakelModelPreferences;
import de.azapps.mirakel.model.MirakelContentObserver;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.recurring.Recurring;
import de.azapps.mirakel.model.semantic.Semantic;
import de.azapps.mirakel.model.tags.Tag;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.new_ui.views.AddTagView;
import de.azapps.mirakel.new_ui.views.DatesView;
import de.azapps.mirakel.new_ui.views.FileView;
import de.azapps.mirakel.new_ui.views.NoteView;
import de.azapps.mirakel.new_ui.views.PriorityChangeView;
import de.azapps.mirakel.new_ui.views.ProgressDoneView;
import de.azapps.mirakel.new_ui.views.ProgressView;
import de.azapps.mirakel.new_ui.views.SubtasksView;
import de.azapps.mirakel.new_ui.views.TagsView;
import de.azapps.mirakelandroid.R;
import de.azapps.widgets.SupportDateTimeDialog;

import static com.google.common.base.Optional.of;
import static de.azapps.tools.OptionalUtils.Procedure;

public class TaskFragment extends DialogFragment implements SoftKeyboard.SoftKeyboardChanged,
    PriorityChangeView.OnPriorityChangeListener, MirakelContentObserver.ObserverCallBack,
    SubtasksView.SubtaskListener, AddTagView.TagChangedListener {

    private static final String TAG = "TaskFragment";
    private static final String TASK = "task";
    public  static final int REQUEST_IMAGE_CAPTURE = 324;
    public static final int FILE_SELECT_CODE = 521;
    public static final String ARGUMENT_TASK = "task";

    private Task task;
    @InjectView(R.id.task_progress_done)
    ProgressDoneView progressDoneView;

    // TaskName
    @InjectView(R.id.task_name)
    TextView taskName;
    @InjectView(R.id.task_name_edit)
    EditText taskNameEdit;
    @InjectView(R.id.task_name_view_switcher)
    ViewSwitcher taskNameViewSwitcher;
    @InjectView(R.id.priority)
    PriorityChangeView priorityChangeView;
    @InjectView(R.id.task_progress)
    ProgressView progressView;

    @InjectView(R.id.task_note)
    NoteView noteView;
    @InjectView(R.id.task_dates)
    DatesView datesView;
    @InjectView(R.id.task_tags)
    TagsView taskTags;
    @InjectView(R.id.task_subtasks)
    SubtasksView subtasksView;
    @InjectView(R.id.task_files)
    FileView filesView;
    @InjectView(R.id.task_tag_add_view)
    AddTagView tagView;

    @InjectView(R.id.task_button_add_more)
    Button addMoreButton;
    PopupMenu addMorePopup;

    @InjectView(R.id.tag_wrapper)
    LinearLayout tagWrapper;
    @InjectView(R.id.note_wrapper)
    LinearLayout noteWrapper;
    @InjectView(R.id.subtask_wrapper)
    LinearLayout subtaskWrapper;
    @InjectView(R.id.file_wrapper)
    LinearLayout fileWrapper;

    private MirakelContentObserver observer;
    private int hiddenViews = 3;
    private SoftKeyboard keyboard;
    private boolean taskNameInitialized = false;

    @Override
    public void onSoftKeyboardHide() {

    }

    @Override
    public void onSoftKeyboardShow() {

    }

    @Override
    public void handleChange() {
        updateTask();
    }

    @Override
    public void handleChange(final long id) {
        if (id == task.getId()) {
            updateTask();
        }
    }


    public static TaskFragment newInstance(final Task task) {
        final TaskFragment taskFragment = new TaskFragment();
        // Supply num input as an argument.
        final Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_TASK, task);
        taskFragment.setArguments(args);
        return taskFragment;
    }

    private void updateTask() {
        final Optional<Task> taskOptional = Task.get(task.getId());
        if (taskOptional.isPresent()) {
            task = taskOptional.get();
        } // else do nothing
        updateAll();

    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TASK, task);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, ThemeManager.getDialogTheme());
        Locale.setDefault(Helpers.getLocale(getActivity()));
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            task = savedInstanceState.getParcelable(TASK);

        } else {
            final Bundle arguments = getArguments();
            task = arguments.getParcelable(ARGUMENT_TASK);
        }
        setRetainInstance(true);
        setContentObserver();
    }

    private void setContentObserver() {
        unregisterContentObserver();
        observer = new MirakelContentObserver(new Handler(Looper.getMainLooper()), getActivity(), Task.URI,
                                              this);
    }

    private void unregisterContentObserver() {
        if ((observer != null) && (getActivity() != null) && (getActivity().getContentResolver() != null)) {
            getActivity().getContentResolver().unregisterContentObserver(observer);
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        unregisterContentObserver();
        super.onDismiss(dialog);
        final boolean appliedSemantics = applySemantics();
        if (!appliedSemantics && task.isStub() && task.getName().equals(getString(R.string.task_new))) {
            task.destroy();
        } else {
            task.save();
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_task, container, false);
        keyboard = new SoftKeyboard((ViewGroup) layout);
        keyboard.setSoftKeyboardCallback(this);
        ButterKnife.inject(this, layout);
        updateAll();
        setupAddMore();

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        AnalyticsWrapperBase.setScreen(this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == Activity.RESULT_OK)) {
            filesView.addPhoto();
        } else if ((requestCode == FILE_SELECT_CODE) && (resultCode == Activity.RESULT_OK)) {
            filesView.addFile(data.getData());
        }
    }

    private void setupAddMore() {
        hiddenViews = 4;
        addMorePopup = new PopupMenu(getActivity(), addMoreButton);
        addMorePopup.inflate(R.menu.add_more_menu);
        final Menu m = addMorePopup.getMenu();
        if (task.getContent().isEmpty()) {
            noteWrapper.setVisibility(View.GONE);
        } else {
            checkDisableAddButton();
            disableItem(m, R.id.add_note_menu);
        }
        if (task.getFiles().isEmpty()) {
            fileWrapper.setVisibility(View.GONE);
        } else {
            checkDisableAddButton();
            disableItem(m, R.id.add_file_menu);
        }
        if (task.getSubtasks().isEmpty()) {
            subtaskWrapper.setVisibility(View.GONE);
        } else {
            checkDisableAddButton();
            disableItem(m, R.id.add_subtask_menu);
        }
        if (task.getTags().isEmpty()) {
            tagWrapper.setVisibility(View.GONE);
        } else {
            checkDisableAddButton();
            disableItem(m, R.id.add_tags_menu);
        }
        addMorePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                item.setVisible(false);
                switch (item.getItemId()) {
                case R.id.add_note_menu:
                    noteWrapper.setVisibility(View.VISIBLE);
                    noteView.handleEditNote();
                    break;
                case R.id.add_subtask_menu:
                    subtaskWrapper.setVisibility(View.VISIBLE);
                    subtasksView.handleAddSubtask();
                    break;
                case R.id.add_tags_menu:
                    tagWrapper.setVisibility(View.VISIBLE);
                    tagView.onClick(tagView);
                    break;
                case R.id.add_file_menu:
                    fileWrapper.setVisibility(View.VISIBLE);
                    filesView.addFile();
                    break;
                }
                checkDisableAddButton();
                return false;
            }
        });
    }

    private static void disableItem(final Menu m, final int id) {
        final MenuItem item = m.findItem(id);
        if (item != null) {
            item.setVisible(false);
        }
    }


    @OnClick(R.id.task_button_add_more)
    void addMore() {
        addMorePopup.show();
    }

    private void checkDisableAddButton() {
        if (--hiddenViews < 1) {
            addMoreButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        if ((getDialog() != null) && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void updateAll() {
        ///////////////////
        // Now the actions
        progressDoneView.setProgress(task.getProgress());
        progressDoneView.setChecked(task.isDone());
        progressDoneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                task.setDone(isChecked);
                task.save();
            }
        });
        taskName.setText(task.getName());
        initTaskNameEdit();
        progressView.setProgress(task.getProgress());
        progressView.setOnProgressChangeListener(progressChangedListener);
        noteView.setNote(task.getContent());
        noteView.setOnNoteChangedListener(noteChangedListener);
        datesView.setData(task);
        datesView.setListeners(dueEditListener, listEditListener, reminderEditListener,
                               dueRecurrenceEditListener, reminderRecurrenceEditListener);
        priorityChangeView.setPriority(task.getPriority());
        priorityChangeView.setOnPriorityChangeListener(this);
        taskTags.setTags(task.getTags());
        taskTags.setTagChangedListener(this);
        subtasksView.initListeners(this);
        subtasksView.setSubtasks(task.getSubtasks());
        filesView.setFiles(task);
        filesView.setActivity(getActivity());
    }

    @Override
    public void onTagAdded(final Tag tag) {
        task.addTag(tag);
        AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.ADD_TAG);
    }

    @Override
    public void onTagRemoved(final Tag tag) {
        task.removeTag(tag);
    }


    @Override
    public void priorityChanged(int priority) {
        task.setPriority(priority);
        task.save();
        AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.SET_PRIORITY);
    }

    private final Procedure<Integer> progressChangedListener = new
    Procedure<Integer>() {
        @Override
        public void apply(final Integer input) {
            if (task.getProgress() == 0 && input > 0) {
                AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.SET_PROGRESS);
            }
            task.setProgress(input);
            task.save();
        }
    };

    @OnEditorAction(R.id.task_name_edit)
    boolean onEditorAction(int actionId) {
        switch (actionId) {
        case EditorInfo.IME_ACTION_DONE:
        case EditorInfo.IME_ACTION_SEND:
            updateName();
            return true;
        }
        return false;
    }

    private void initTaskNameEdit() {
        taskNameEdit.setText(task.getName());
        // Show Keyboard if stub
        if (task.isStub() && !taskNameInitialized) {
            taskNameInitialized = true;
            taskNameViewSwitcher.showNext();
            taskNameEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    taskNameEdit.requestFocus();
                    taskNameEdit.selectAll();
                }
            }, 10L);

        }
        taskNameEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    updateName();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateName() {
        taskNameEdit.clearFocus();
        applySemantics();
        taskNameEdit.setText(task.getName());
        task.save();
        taskNameViewSwitcher.showPrevious();
    }

    @OnClick(R.id.task_name)
    void clickTaskName() {
        taskNameViewSwitcher.showNext();
        taskNameEdit.setText(task.getName());
        taskNameEdit.requestFocus();
    }

    private final Procedure<String> noteChangedListener = new
    Procedure<String>() {
        @Override
        public void apply(final String input) {
            if (task.getContent().isEmpty() && !input.isEmpty()) {
                AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.ADD_NOTE);
            }
            task.setContent(input);
            task.save();
        }
    };
    private final View.OnClickListener dueEditListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final SupportDatePickerDialog datePickerDialog = SupportDatePickerDialog.newInstance(new
            DatePicker.OnDateSetListener() {
                @Override
                public void onDateSet(final DatePicker datePickerDialog, final int year, final int month,
                                      final int day) {
                    task.setDue(of((Calendar) new GregorianCalendar(year, month, day)));
                    task.save();
                    AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.SET_DUE);
                }

                @Override
                public void onNoDateSet() {
                    task.setDue(Optional.<Calendar>absent());
                    task.save();
                }
            }, task.getDue(), false);
            datePickerDialog.show(getFragmentManager(), "dueDialog");
        }
    };

    private final View.OnClickListener listEditListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final ArrayAdapter<ListMirakel> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, ListMirakel.all(false));
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.task_move_to);
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    task.setList(adapter.getItem(i));
                    task.save();
                }
            });
            builder.show();
        }
    };
    private final View.OnClickListener reminderEditListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final SupportDateTimeDialog dateTimeDialog = SupportDateTimeDialog.newInstance(
            new SupportDateTimeDialog.OnDateTimeSetListener() {
                @Override
                public void onDateTimeSet(final int year, final int month, final int dayOfMonth,
                                          final int hourOfDay, final int minute) {
                    AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.SET_REMINDER);
                    task.setReminder(of((Calendar) new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute)));
                    task.save();
                }

                @Override
                public void onNoTimeSet() {
                    task.setReminder(Optional.<Calendar>absent());
                    task.save();
                }
            }, task.getReminder());
            dateTimeDialog.show(getFragmentManager(), "reminderDialog");
        }
    };


    private final View.OnClickListener dueRecurrenceEditListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            RecurrencePickerDialog rp = RecurrencePickerDialog.newInstance(new
            RecurrencePickerDialog.OnRecurrenceSetListener() {

                @Override
                public void onRecurrenceSet(@NonNull Optional<Recurring> r) {
                    task.setRecurrence(r);
                    task.save();
                }
            }, task.getRecurrence(), true, false);
            rp.show(getFragmentManager(), "recurrencePickerDue");
        }
    };

    private final View.OnClickListener reminderRecurrenceEditListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            RecurrencePickerDialog rp = RecurrencePickerDialog.newInstance(new
            RecurrencePickerDialog.OnRecurrenceSetListener() {

                @Override
                public void onRecurrenceSet(@NonNull final Optional<Recurring> r) {
                    if (r.isPresent()) {
                        AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.SET_RECURRING_REMINDER);
                    }
                    task.setRecurringReminder(r);
                    task.save();
                }
            }, task.getRecurringReminder(), false, true);
            rp.show(getFragmentManager(), "recurrencePickerReminder");
        }
    };

    private boolean applySemantics() {
        return Semantic.applySemantics(task, taskNameEdit.getText().toString());
    }

    @OnClick(R.id.task_button_done)
    void doneClick() {
        applySemantics();
        task.save();
        taskNameEdit.clearFocus();
        tagView.clearFocus();
        dismiss();
    }



    @Override
    public void onAddSubtask(String taskName) {
        final ListMirakel list = MirakelModelPreferences
                                 .getListForSubtask(task);
        final Task subtask = Semantic.createTask(taskName, Optional.fromNullable(list),
                             true);
        task.addSubtask(subtask);
        task.save();
        AnalyticsWrapperBase.track(AnalyticsWrapperBase.ACTION.ADD_SUBTASK);
    }

    @Override
    public void onSubtaskClick(Task subtask) {
        final DialogFragment newFragment = TaskFragment.newInstance(subtask);
        newFragment.show(getFragmentManager(), "dialog");

    }

    @Override
    public void onSubtaskDone(Task subtask, boolean done) {
        subtask.setDone(done);
        subtask.save();

    }


}

