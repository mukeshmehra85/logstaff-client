/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package customList;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import logstaff.model.Project;

/**
 *
 * @author Mukesh
 */
public class ListRow extends ListCell<Project>{
    private Label title = new Label();
    private Label time = new Label();

    public ListRow() {
    }
    
    @Override
    protected void updateItem(Project item,boolean empty){
        super.updateItem(item, empty);
        if(item!=null){
            setText(item.getTitle());
        }else{
            setText("");
        }
    }
}
