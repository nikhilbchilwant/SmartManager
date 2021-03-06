package mas.maintenanceproxy.gui.preventive;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import mas.maintenanceproxy.classes.PMaintenance;

/**
 * @author Anand Prajapati
 * <p>
 * Table Model for table of done preventive maintenance activities
 * </p>
 *
 */
public class PrevMaintTableModel extends AbstractTableModel implements TableModel {

	private static final long serialVersionUID = 1L;
	ArrayList<PMaintenance> maintSchedules;

	public PrevMaintTableModel() {
		this.maintSchedules = new ArrayList<PMaintenance>();
	}

	@Override
	public Class<?> getColumnClass(int index) {
		return PMaintenance.class ;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int arg0) {
		return "<html><h2>Completed Preventive Maintenance</h2></html>";
	}

	@Override
	public int getRowCount() {
		return maintSchedules.size();	
	}

	@Override
	public Object getValueAt(int tileIndex, int columnIndex) {
		return maintSchedules.get(tileIndex);
	}

	@Override
	public boolean isCellEditable(int columnIndex, int rowIndex) {
		return false;
	}

	public void addMaintJob(PMaintenance pm){
		maintSchedules.add(0, pm);

		super.fireTableRowsInserted(0, getRowCount()-1);
		super.fireTableCellUpdated(0, getRowCount()-1);
		super.fireTableDataChanged();
	}

}
