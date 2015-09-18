package training.lesson;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import training.commands.BadCommandException;
import training.commands.Command;
import training.commands.CommandFactory;
import training.commands.ExecutionList;
import training.commandsEx.util.PerformActionUtil;
import training.editor.MouseListenerHolder;
import training.editor.EduEditor;
import training.editor.actions.HideProjectTreeAction;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

    public static void process(final Lesson lesson, final EduEditor eduEditor, final Project project, Document document, @Nullable String target) throws InterruptedException, ExecutionException, BadCommandException {

        HashMap<String, String> editorParameters = new HashMap<String, String>();

        Queue<Element> elements = new LinkedBlockingQueue<Element>();
        if (lesson.getScn().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        final Element root = lesson.getScn().getRoot();

        if (root.equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        //getEditor parameters
        getEditorParameters(root, editorParameters);

        //Create queue of Actions
        for (final Element el : root.getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if(isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        if (isCaretBlock(el1)) {
                            if (el1.getChildren() != null) {
                                elements.add(el1); //add block element
                                for (Element el2 : el1.getChildren()) {
                                    elements.add(el2); //add inner elements
                                }
                                elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                            }
                        } else {
                            elements.add(el1); //add inner elements
                        }
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else if (isCaretBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        MouseListenerHolder mouseListenerHolder = new MouseListenerHolder();

        //Initialize lesson in the EduEditor
        eduEditor.initLesson(lesson);

        //Prepare environment before execution
        prepareEnvironment(eduEditor, project, editorParameters);

        //Perform first action, all next perform like a chain reaction
        Command cmd = CommandFactory.buildCommand(elements.peek());
        ExecutionList executionList = new ExecutionList(elements, lesson, project, eduEditor, mouseListenerHolder, target);

        cmd.execute(executionList);

    }

    private static void getEditorParameters(Element root, HashMap<String, String> editorParameters) {
        if(root.getAttribute(Lesson.EditorParameters.PROJECT_TREE) != null) {
            editorParameters.put(Lesson.EditorParameters.PROJECT_TREE, root.getAttributeValue(Lesson.EditorParameters.PROJECT_TREE));
        }
    }

    private static void prepareEnvironment(EduEditor eduEditor, Project project, HashMap<String, String> editorParameters) throws ExecutionException, InterruptedException {
        if(editorParameters.containsKey(Lesson.EditorParameters.PROJECT_TREE)) {
            if (ActionManager.getInstance().getAction(HideProjectTreeAction.actionId) == null) {
                HideProjectTreeAction hideAction = new HideProjectTreeAction();
                ActionManager.getInstance().registerAction(hideAction.getActionId(), hideAction);
            }
            PerformActionUtil.performAction(HideProjectTreeAction.actionId, eduEditor.getEditor(), project);
        }
    }

    private static boolean isMouseBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.MOUSEBLOCK.toString());
    }

    private static boolean isCaretBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.CARETBLOCK.toString());
    }


}
