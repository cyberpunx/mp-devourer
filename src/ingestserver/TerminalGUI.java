/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.File;
import java.io.IOException;
import static java.nio.file.Files.size;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author ragnarok
 */
public class TerminalGUI {

    Config config;
    String defaultMedia;

    public TerminalGUI() {
        try {
            this.config = new Config();
        } catch (IOException ex) {
            Logger.getLogger(RedisManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.defaultMedia = config.getMediaDirectory();
    }

    public void executeGUI() throws IOException {
        // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        // Creates run mode panel
        Panel runModePanel = new Panel();
        runModePanel.setLayoutManager(new GridLayout(2));
        runModePanel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
        TerminalSize size = new TerminalSize(30, 5);
        ActionListBox actionListBox = new ActionListBox(size);
        actionListBox.addItem("RUN AND DIE", new Runnable() {
            @Override
            public void run() {
                MessageDialog.showMessageDialog(gui, "", "Press <enter> and wait!");
                // Execute Directory crawler
                DirectoryCrawler dc;
                dc = new DirectoryCrawler(config.getMediaDirectory());
                dc.analyze();
                MessageDialog.showMessageDialog(gui, "", "Done!");
                System.exit(0); // Exits when finish
            }
        });
        actionListBox.addItem("START SERVICE", new Runnable() {
            @Override
            public void run() {
                //executes directory poller
                new MessageDialogBuilder()
                        .setTitle("")
                        .setText("Not implemented yet!")
                        .addButton(MessageDialogButton.Close)
                        .build()
                        .showDialog(gui);
            }
        });

        runModePanel.addComponent(actionListBox);

        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.NO_DECORATIONS));

        // creates main panel
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        // create panel to input media directory
        Panel filePanel = new Panel();
        mainPanel.addComponent(filePanel.withBorder(Borders.singleLine("Media Directory")));
        mainPanel.addComponent(runModePanel.withBorder(Borders.singleLine("Run Mode")));
        filePanel.addComponent(new EmptySpace(new TerminalSize(33, 0)));

        filePanel.addComponent(new Button("Enter media directory", new Runnable() {
            @Override
            public void run() {
                try {
                    String input = new TextInputDialogBuilder()
                            .setInitialContent(defaultMedia)
                            .setTitle("Enter path to media directory")
                            .build()
                            .showDialog(gui);
                    if (input == null) {
                        //System.out.println("Cancel button pressed");
                    }
                    if (input.isEmpty()) {
                        // System.out.println("ERROR: Media directory is empty");
                    } else {
                        //System.out.println("input: " + input);
                    }

                    try {
                        config.setProperty("media_directory", input);
                    } catch (IOException ex) {
                        Logger.getLogger(TerminalGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (NullPointerException e) {
                    //System.out.println("ERROR or cancel button");
                }

            }
        }));

        // exit button panel
        Panel exitPanel = new Panel();
        exitPanel.addComponent(new EmptySpace(new TerminalSize(10, 1)));
        exitPanel.addComponent(new Button("Exit", new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }));
        exitPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        mainPanel.addComponent(exitPanel);

        window.setComponent(mainPanel);
        // Create gui and start gui
        gui.addWindowAndWait(window);

    }

}