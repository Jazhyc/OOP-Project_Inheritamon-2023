package inheritamon.view.combat;
import javax.swing.*;
import java.awt.*;

public class DialoguePanel extends JPanel {

    private JTextArea textArea;

    public DialoguePanel() {
        
        // Change the color to red
        setBackground(Color.YELLOW);

    }

    public void setTextToDisplay(String text) {
        textArea.setText(text);
    }
    
    
}
