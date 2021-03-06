
package mas.globalSchedulingproxy.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import com.alee.extended.label.WebHotkeyLabel;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.hotkey.Hotkey;

import mas.globalSchedulingproxy.agent.GlobalSchedulingAgent;
import mas.jobproxy.Batch;
import mas.util.AgentUtil;
/**
 * 
 * Main GUI class for GSA
 * @author NikhilChilwant
 *
 */
public class WebLafGSA {

	private static GlobalSchedulingAgent GSA=null;
	private static JFrame welcomeScreenFrame=null;
	private static BorderLayout layout=null;

	private static WebScrollPane currentJobList=null;
	private static WebScrollPane completedJobsList=null;
	private static WebScrollPane negotiationJobList=null;

	private static WebPanel MainPanel=null;
	
	private static WebToggleButton JobManager;
	private static WebToggleButton About ;
	private static WebToggleButton Negotiation;
	private static WebToggleButton completedJobs;

	//info about selected job tile on right side
	private static WebPanel currentJobListinfoPanel=null;
	private static WebPanel completedJobListinfoPanel=null;
	private static WebPanel NegotiationJobListinfoPanel=null;

	public enum notificationType{
		error, newJob, completedJob, negotiationBid
	}
	private static Logger log=LogManager.getLogger();


	//table of jobTiles. Left side of frame
	private static JTable currentJobListTable=null;
	private static JTable negotiationJobListTable=null;
	private static JTable completedJobListTable=null;

	private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static double width = screenSize.getWidth();
	private static double height = screenSize.getHeight();

	public static TrayIcon GSAguiIcon ;
	private static WebToggleButton[] bottomButtons;
	private SystemTray tray;
	private static ImageIcon smartManagerWinIcon;

	public WebLafGSA(GlobalSchedulingAgent globalSchedulingAgent){
		this.GSA=globalSchedulingAgent;
		init();
	}

	public void dispose() {
		if(welcomeScreenFrame != null) {
			welcomeScreenFrame.dispose();
		}

	}

	private void init() {

		Image image = Toolkit.getDefaultToolkit().getImage("resources/smartMachine.png");
		GSAguiIcon= new TrayIcon(image, "GSA");
		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();

			GSAguiIcon.setImageAutoSize(true);
			try {
				tray.add(GSAguiIcon);
			} catch (AWTException e) {
				log.info("TrayIcon could not be added.");
			}
		}

		this.welcomeScreenFrame=new WebFrame("Smart Manager :: "+"Shop Floor Manager");
//		welcomeScreenFrame.setResizable(false);
		smartManagerWinIcon = new ImageIcon("resources/smartManager.png","Logo icon");
		welcomeScreenFrame.setIconImage(smartManagerWinIcon.getImage());
		this.layout=new BorderLayout();
		this.MainPanel=new WebPanel(layout);
		//		this.MainPanel.setOpaque(false);

		currentJobListinfoPanel=new WebPanel(new MigLayout());
//		currentJobListinfoPanel.setBackground(Color.decode("#ffebe3"));
		//		currentJobListinfoPanel.setOpaque(false);
		completedJobListinfoPanel=new WebPanel(new MigLayout());
//		completedJobListinfoPanel.setBackground(Color.decode("#f1f7fd"));
		//		completedJobListinfoPanel.setOpaque(false);
		NegotiationJobListinfoPanel=new WebPanel(new MigLayout());
//		NegotiationJobListinfoPanel.setBackground(Color.decode("#f7fdf1"));
		
		//		NegotiationJobListinfoPanel.setOpaque(false);



		/*currentJobListinfoPanel.setBackground(Color.RED);
		completedJobListinfoPanel.setBackground(Color.BLUE);
		NegotiationJobListinfoPanel.setBackground(Color.GREEN);*/

		initCompletedJobListPanel();
		initCurrentJobListPanel();
		initNegotiationListPanel();

//		WebPanel menu = new WebPanel ( new HorizontalFlowLayout ( 5, false ) );
		WebPanel menu=new WebPanel(new MigLayout());
//		 WebPanel menu = new WebPanel ( new HorizontalFlowLayout ( 5, false ) );
		menu.setPreferredSize(new Dimension((int)width, 100));
		bottomButtons=getButtons();
		
		ImageIcon img2 = new ImageIcon("resources/smartManagerBanner.png");
		JLabel logo_label = new JLabel(""
				, img2, JLabel.CENTER);
//		JPanel panel = new JPanel(new BorderLayout());
//		panel.add( label, BorderLayout.CENTER );
		menu.add(logo_label, FlowLayout.LEFT);
		
		for(int i=0;i<bottomButtons.length;i++){
			menu.add(bottomButtons[i]);	
		}
//		ButtonGroup bg=SwingUtils.groupButtons ( menu );
		MainPanel.add(menu, BorderLayout.SOUTH);
//		MainPanel.add(currentJobList,BorderLayout.WEST);

		welcomeScreenFrame.add(MainPanel);
		welcomeScreenFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		welcomeScreenFrame.setVisible(true);
		bottomButtons[2].doClick();
		
		welcomeScreenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected static void initCurrentJobListPanel() {
		CurrentJobTableModel currJobTileRenderer= new CurrentJobTableModel();
		currentJobListTable=new JTable(currJobTileRenderer);
		currentJobListTable.setDefaultRenderer(JobTile.class, new CurrentJobTableRenderer());
		currentJobListTable.setDefaultEditor(JobTile.class, new CurrentJobTableRenderer());
		currentJobListTable.setRowHeight(80);
		currentJobListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		currentJobList=new WebScrollPane(currentJobListTable);
		currentJobList.setPreferredWidth(350);
	}

	private void initCompletedJobListPanel() {
		CompletedJobTableModel completedJobRenderer= new CompletedJobTableModel();
		completedJobListTable=new JTable(completedJobRenderer);

		completedJobListTable.setDefaultRenderer(JobTile.class, new CompletedJobTableRenderer());
		completedJobListTable.setDefaultEditor(JobTile.class, new CompletedJobTableRenderer());
		completedJobListTable.setRowHeight(90);

		completedJobsList=new WebScrollPane(completedJobListTable);
		completedJobsList.setPreferredWidth(350);
	}

	private void initNegotiationListPanel() {

		NegotiationJobTileTableModel negotiationRenderer= new NegotiationJobTileTableModel();
		negotiationJobListTable=new JTable(negotiationRenderer);
		negotiationJobListTable.setDefaultRenderer(JobTile.class, new NegotitationJobTileCellRenderer());
		negotiationJobListTable.setDefaultEditor(JobTile.class, new NegotitationJobTileCellRenderer());
		negotiationJobListTable.setRowHeight(80);

		negotiationJobList=new WebScrollPane(negotiationJobListTable);
		negotiationJobList.setPreferredWidth(350);

	}

	public static void unloadCurrentJobInfoPanel() {
		currentJobListinfoPanel.removeAll();
		MainPanel.remove(currentJobListinfoPanel);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);
	}

	public static void unloadCompletedJobInfoPanel() {
		completedJobListinfoPanel.removeAll();
		MainPanel.remove(completedJobListinfoPanel);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);
	}

	public static void unloadNegotiationInfoPanel() {
		NegotiationJobListinfoPanel.removeAll();
		MainPanel.remove(NegotiationJobListinfoPanel);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);
	}

	public static void createCurrentJobInfoPanel(JobTile jobToShow){
		MigLayout migLayout=new MigLayout("","200","[30]10");
		WebPanel detailsPanel=new WebPanel(migLayout);

		final Format formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

		WebLabel JobNametextlbl,JobIDTxtlbl,jobCustStartDateTxtlbl,jobCustDueDateTextlbl, customerTxtlbl,
		jobGSAStartDateTxtlbl,jobGSADueDateTxtlbl, durationTextlbl, priorityTextlbl;

		WebHotkeyLabel JobNamelbl,JobIDlbl,jobCustStartDatelbl,jobCustDueDatelbl,
		jobGSAStartDatelbl,jobGSADueDatelbl, durationlbl, prioritylbl, customerlbl;

		Font TextlblFont=UIManager.getDefaults().getFont("TabbedPane.font");
		//		TextlblFont=TextlblFont.deriveFont(Font.PLAIN, 12);
		Font lblFont = TextlblFont.deriveFont(Font.PLAIN, 20);

//		JobNametextlbl=new WebLabel("Job Name");
//		JobNamelbl=new WebHotkeyLabel(jobToShow.getJobName());
		//		JobNamelbl.setMinimumWidth(200);

		JobIDTxtlbl=new WebLabel("Order ID");
		JobIDlbl=new WebHotkeyLabel(jobToShow.getBatchID().toString());
		//		JobIDlbl.setMinimumWidth(150);

		customerTxtlbl=new WebLabel("Customer ID");
		customerlbl=new WebHotkeyLabel(jobToShow.getCustomerName());
		
		jobCustStartDateTxtlbl=new WebLabel("Order Generation Date");
		jobCustStartDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getCustStartDate()));
		//		jobCustStartDatelbl.setMinimumWidth(150);

		jobCustDueDateTextlbl=new WebLabel("Due Date by customer");
		jobCustDueDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getCustDueDate()));
		//		jobCustDueDatelbl.setMinimumWidth(150);

//		jobGSAStartDateTxtlbl=new WebLabel("Start date by MAS");
//		jobGSAStartDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getStartDatebyGSA()));
//		//		jobGSAStartDatelbl.setMinimumWidth(150);
//
//		jobGSADueDateTxtlbl=new WebLabel("Due date by MAS");
//		jobGSADueDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getDueDatebyGSA()));
		//		jobGSADueDatelbl.setMinimumWidth(150);

//		durationTextlbl=new WebLabel("Duration (seconds)");
//		durationlbl=new WebHotkeyLabel(Double.toString(jobToShow.getProcessingTime()));
		//		durationlbl.setMinimumWidth(150);

		priorityTextlbl=new WebLabel("Customer Priority");
		prioritylbl=new WebHotkeyLabel(AgentUtil.showPriority(jobToShow.getPriority()));
		//		prioritylbl.setMinimumWidth(150);

		/*		

		JobNametextlbl.setFont(TextlblFont);
		JobIDTxtlbl.setFont(TextlblFont);
		jobCustStartDateTxtlbl.setFont(TextlblFont);
		jobCustDueDateTextlbl.setFont(TextlblFont);
		durationTextlbl.setFont(TextlblFont);
		priorityTextlbl.setFont(TextlblFont);*/

//		JobNamelbl.setFont(lblFont);
		JobIDlbl.setFont(lblFont);
		jobCustStartDatelbl.setFont(lblFont);
		jobCustDueDatelbl.setFont(lblFont);
//		durationlbl.setFont(lblFont);
		prioritylbl.setFont(lblFont);
		customerlbl.setFont(lblFont);
//		jobGSAStartDatelbl.setFont(lblFont);
//		jobGSADueDatelbl.setFont(lblFont);

//		detailsPanel.add(JobNametextlbl,"growx");
		
		detailsPanel.add(customerTxtlbl,"growx");
		detailsPanel.add(customerlbl,"wrap, growx");
		
		detailsPanel.add(priorityTextlbl,"growx");
		detailsPanel.add(prioritylbl,"wrap, growx");
		
		detailsPanel.add(JobIDTxtlbl,"growx");
		detailsPanel.add(JobIDlbl,"wrap, growx");
		
		detailsPanel.add(jobCustStartDateTxtlbl,"growx");
		detailsPanel.add(jobCustStartDatelbl,"wrap, growx");
		
		detailsPanel.add(jobCustDueDateTextlbl,"growx");
		detailsPanel.add(jobCustDueDatelbl,"wrap, growx");

//		detailsPanel.add(jobCustDueDatelbl,"wrap, growx");

//		detailsPanel.add(durationTextlbl,"growx");
//		detailsPanel.add(durationlbl,"wrap, growx");
		

//		detailsPanel.add(jobGSAStartDateTxtlbl,"growx");
//		detailsPanel.add(jobGSADueDateTxtlbl,"wrap, growx");
		
		
//		detailsPanel.add(jobGSAStartDatelbl,"growx");
//		detailsPanel.add(jobGSADueDatelbl,"wrap, growx");

		currentJobListinfoPanel.add(detailsPanel,"wrap");

		//		JScrollPane jobTable=createsJobsTable(); //jobs in batch

		//		infoPanel.add(jobTable);

		MainPanel.add(currentJobListinfoPanel,BorderLayout.CENTER);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);


	}

	public static void creatCompletedJobInfoPanel(JobTile jobTileInCell) {
		MigLayout migLayout=new MigLayout("","200","[30]");
		WebPanel detailsPanel=new WebPanel(migLayout);

		final Format formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

		WebLabel JobNametextlbl,JobIDTxtlbl,jobCustStartDateTxtlbl,jobCustDueDateTextlbl, customerTxtlbl,
		jobGSAStartDateTxtlbl,jobGSADueDateTxtlbl, durationTextlbl, priorityTextlbl, actualCompletionTxtlbl;

		WebHotkeyLabel JobNamelbl,JobIDlbl,jobCustStartDatelbl,jobCustDueDatelbl,
		jobGSAStartDatelbl,jobGSADueDatelbl, durationlbl, prioritylbl, customerlbl, actualCompletionlbl;

		Font TextlblFont=UIManager.getDefaults().getFont("TabbedPane.font");
		//		TextlblFont=TextlblFont.deriveFont(Font.PLAIN, 12);
		Font lblFont = TextlblFont.deriveFont(Font.PLAIN, 20);

//		JobNametextlbl=new WebLabel("Job Name");
//		JobNamelbl=new WebHotkeyLabel(jobToShow.getJobName());
		//		JobNamelbl.setMinimumWidth(200);

		JobIDTxtlbl=new WebLabel("Job ID");
		JobIDlbl=new WebHotkeyLabel(jobTileInCell.getBatchID().toString());
		//		JobIDlbl.setMinimumWidth(150);

		customerTxtlbl=new WebLabel("Customer");
		customerlbl=new WebHotkeyLabel(jobTileInCell.getCustomerName());
		
		jobCustStartDateTxtlbl=new WebLabel("Start Date by customer");
		jobCustStartDatelbl=new WebHotkeyLabel(formatter.format(jobTileInCell.getCustStartDate()));
		//		jobCustStartDatelbl.setMinimumWidth(150);

		jobCustDueDateTextlbl=new WebLabel("Due Date by customer");
		jobCustDueDatelbl=new WebHotkeyLabel(formatter.format(jobTileInCell.getCustDueDate()));
		
		actualCompletionTxtlbl=new WebLabel("Manufacturing completed on");
		actualCompletionlbl=new WebHotkeyLabel(formatter.format(
				jobTileInCell.getActualOrderCompletionTime()));
		//		jobCustDueDatelbl.setMinimumWidth(150);

//		jobGSAStartDateTxtlbl=new WebLabel("Start date by MAS");
//		jobGSAStartDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getStartDatebyGSA()));
//		//		jobGSAStartDatelbl.setMinimumWidth(150);
//
//		jobGSADueDateTxtlbl=new WebLabel("Due date by MAS");
//		jobGSADueDatelbl=new WebHotkeyLabel(formatter.format(jobToShow.getDueDatebyGSA()));
		//		jobGSADueDatelbl.setMinimumWidth(150);

		durationTextlbl=new WebLabel("Duration (seconds)");
		durationlbl=new WebHotkeyLabel(Double.toString(jobTileInCell.getProcessingTime()/1000.0));
		//		durationlbl.setMinimumWidth(150);

		priorityTextlbl=new WebLabel("Priority");
		prioritylbl=new WebHotkeyLabel(AgentUtil.showPriority(jobTileInCell.getPriority()));
		//		prioritylbl.setMinimumWidth(150);

		/*		

		JobNametextlbl.setFont(TextlblFont);
		JobIDTxtlbl.setFont(TextlblFont);
		jobCustStartDateTxtlbl.setFont(TextlblFont);
		jobCustDueDateTextlbl.setFont(TextlblFont);
		durationTextlbl.setFont(TextlblFont);
		priorityTextlbl.setFont(TextlblFont);*/

//		JobNamelbl.setFont(lblFont);
		JobIDlbl.setFont(lblFont);
		jobCustStartDatelbl.setFont(lblFont);
		jobCustDueDatelbl.setFont(lblFont);
		durationlbl.setFont(lblFont);
		prioritylbl.setFont(lblFont);
		customerlbl.setFont(lblFont);
		actualCompletionlbl.setFont(lblFont);
//		jobGSAStartDatelbl.setFont(lblFont);
//		jobGSADueDatelbl.setFont(lblFont);

//		detailsPanel.add(JobNametextlbl,"growx");
		detailsPanel.add(JobIDTxtlbl,"growx");
		detailsPanel.add(JobIDlbl,"wrap, growx");
		
		detailsPanel.add(customerTxtlbl,"growx");
		detailsPanel.add(customerlbl,"wrap, growx");
		
		detailsPanel.add(jobCustStartDateTxtlbl,"growx");
		detailsPanel.add(jobCustStartDatelbl,"wrap, growx");
		
		detailsPanel.add(jobCustDueDateTextlbl,"growx");
		detailsPanel.add(jobCustDueDatelbl,"wrap, growx");
		
		detailsPanel.add(actualCompletionTxtlbl,"growx");
		detailsPanel.add(actualCompletionlbl,"wrap, growx");

//		detailsPanel.add(jobCustDueDatelbl,"wrap, growx");

		detailsPanel.add(durationTextlbl,"growx");
		detailsPanel.add(durationlbl,"wrap, growx");
		
		detailsPanel.add(priorityTextlbl,"growx");
		detailsPanel.add(prioritylbl,"wrap, growx");
		
//		detailsPanel.add(jobGSAStartDateTxtlbl,"growx");
//		detailsPanel.add(jobGSADueDateTxtlbl,"wrap, growx");

		
		
//		detailsPanel.add(jobGSAStartDatelbl,"growx");
//		detailsPanel.add(jobGSADueDatelbl,"wrap, growx");

		
		completedJobListinfoPanel.add(detailsPanel,"wrap");

		MainPanel.add(completedJobListinfoPanel,BorderLayout.CENTER);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);

	}

	public static void creatNegotiationInfoPanel(JobTile jobTileInCell) {

		NegotiationInfo nip=new NegotiationInfo(GSA, jobTileInCell.getBatch());
		NegotiationJobListinfoPanel=nip.getPanel();
		MainPanel.add(NegotiationJobListinfoPanel,BorderLayout.CENTER);
		welcomeScreenFrame.validate();
		welcomeScreenFrame.repaint();
		welcomeScreenFrame.setVisible(true);
	}


	private static WebToggleButton[] getButtons(){
		
		WebToggleButton[] buttons=new WebToggleButton[4];
		About = new WebToggleButton();
		Image img = null;
		try {
			img = ImageIO.read (new File("resources/about.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println(img);
		About.setIcon(new ImageIcon(img));
		About.setPreferredSize(new Dimension(90,90));
		About.setActionCommand("about");
		About.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
	                untoggleAllBottonButtons();
					About.setSelected(true);
					createAndShowAboutWindow();
					 untoggleAllBottonButtons();
			}
		});  
		About.setRolloverDecoratedOnly ( true );
		About.setDrawFocus (false);
		buttons[0]=About;

		Negotiation = new WebToggleButton();
		Image negotiationImg = null;
		try {
			negotiationImg = ImageIO.read (new File("resources/negotiation.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Negotiation.setIcon(new ImageIcon( negotiationImg));
		Negotiation.setPreferredSize(new Dimension(90,90));
		Negotiation.setActionCommand("negotition");
		Negotiation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
                untoggleAllBottonButtons();
				Negotiation.setSelected(true);
				
				cleanMainPanel();
				MainPanel.add(negotiationJobList,BorderLayout.WEST);
				welcomeScreenFrame.revalidate();
				welcomeScreenFrame.repaint();
				welcomeScreenFrame.setVisible(true);
			}
		});
		
		Negotiation.setRolloverDecoratedOnly ( true );
		Negotiation.setDrawFocus (false);
		buttons[1]=Negotiation;

		JobManager = new WebToggleButton();
		Image JobManagerImg = null;
		try {
			JobManagerImg = ImageIO.read (new File("resources/JobManager.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JobManager.setIcon(new ImageIcon( JobManagerImg));
		JobManager.setPreferredSize(new Dimension(90,90));
		JobManager.setActionCommand("jobManager");

		JobManager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
                untoggleAllBottonButtons();
                JobManager.setSelected(true);
				
				cleanMainPanel();

				MainPanel.add(currentJobList,BorderLayout.WEST);
				welcomeScreenFrame.revalidate();
				welcomeScreenFrame.repaint();
				welcomeScreenFrame.setVisible(true);
			}


		});  
		
		JobManager.setRolloverDecoratedOnly ( true );
		JobManager.setDrawFocus (false);
		
		buttons[2]=JobManager;



		completedJobs = new WebToggleButton();
		Image CompletedJobsImg = null;
		try {
			CompletedJobsImg = ImageIO.read (new File("resources/completedJob.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		completedJobs.setIcon(new ImageIcon( CompletedJobsImg));
		completedJobs.setPreferredSize(new Dimension(90,90));
		completedJobs.setActionCommand("completedJob");

		completedJobs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
                untoggleAllBottonButtons();
                completedJobs.setSelected(true);
				cleanMainPanel();

				MainPanel.add(completedJobsList,BorderLayout.WEST);
				welcomeScreenFrame.revalidate();
				welcomeScreenFrame.repaint();
				welcomeScreenFrame.setVisible(true);
			}
		});

		completedJobs.setRolloverDecoratedOnly ( true );
		completedJobs.setDrawFocus (false);
		buttons[3]=completedJobs;

		return buttons;
	}

	protected static void createAndShowAboutWindow() {
				ImageIcon showProfileIcon = new ImageIcon("resources/linkedIn_viewmy_120x33.png",
						"View LinkedIn profile");
				JFrame developersFrame = new JFrame("Developers");
				developersFrame.setResizable(false);
				developersFrame.setLayout(new MigLayout());
				developersFrame.setIconImage(smartManagerWinIcon.getImage());	
				
				JPanel devCreditPanel = new JPanel(new MigLayout("",
						"",
						"10[10]"));
//				devCreditPanel.add(new WebHotkeyLabel("<html><b>Core developers</b></html>"),"wrap");
				devCreditPanel.add(new JLabel("<html><p>Nikhil Chilwant</p></html>"));

				JButton NikhilViewProfile = new JButton();
				NikhilViewProfile.setIcon(showProfileIcon);
				NikhilViewProfile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e){
						URI linkToOpen = null;
						try {
							linkToOpen = new URI("https://in.linkedin.com/pub/nikhil-chilwant/98/676/973");
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
						openWebpage(linkToOpen);
				}
			});  
				devCreditPanel.add( NikhilViewProfile,"wrap");
				
				devCreditPanel.add(new JLabel("<html><p>Anand Prajapati</p></html>"));
				JButton AnandViewProfile = new JButton();
				AnandViewProfile.setIcon(showProfileIcon);
				AnandViewProfile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e){
						URI linkToOpen = null;
						try {
							linkToOpen = new URI("https://in.linkedin.com/in/akprats");
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
						openWebpage(linkToOpen);
				}
			}); 
				
				devCreditPanel.add(AnandViewProfile,"wrap");
				devCreditPanel.add(new JLabel("<html>Visit <b>http://smartmanager.bitbucket.org</b>"
						+ " for more details.</html>"),"span 2");

				developersFrame.add(devCreditPanel);
				developersFrame.setMinimumSize(new Dimension(450,200));
				
				int centerX = (int)screenSize.getWidth() / 2;
				int centerY = (int)screenSize.getHeight() / 2;
				developersFrame.setLocation(centerX - developersFrame.getWidth() / 2, 
						centerY - developersFrame.getHeight() / 2);
				developersFrame.setVisible(true);
				
            	URI linkToOpen = null;
				try {
					linkToOpen = new URI("http://smartmanager.bitbucket.org");
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
				openWebpage(linkToOpen);
		
	}
	
	private static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	private static void untoggleAllBottonButtons(){
		for(int i=0;i<bottomButtons.length;i++){
			bottomButtons[i].setSelected(false);
		}
	}
	
	public static JTable getNegotiationJobListTable() {
		return negotiationJobListTable;
	}

	public static JFrame getWelcomeScreenFrame() {
		return welcomeScreenFrame;
	}

	public static GlobalSchedulingAgent getGSA() {
		return GSA;
	}

	protected static void cleanMainPanel() {
		if(completedJobsList!=null && completedJobsList.getRootPane()!=null){
			MainPanel.remove(completedJobsList);
			MainPanel.remove(completedJobListinfoPanel);
		}
		else if(negotiationJobList!=null && negotiationJobList.getRootPane()!=null){
			MainPanel.remove(negotiationJobList);
			MainPanel.remove(NegotiationJobListinfoPanel);
		}
		else if(currentJobList!=null && currentJobList.getRootPane()!=null){
			MainPanel.remove(currentJobList);
			MainPanel.remove(currentJobListinfoPanel);
		}
	}


	public void addCompletedJob(Batch b) {
		CurrentJobTableModel CurrjobListRenderer=
				(CurrentJobTableModel)currentJobListTable.getModel();
		
		if(currentJobListTable.getCellEditor() != null ) {
			currentJobListTable.getCellEditor().stopCellEditing(); 
			//solves cell updating bug
		}
		CurrjobListRenderer.removeJob(b);

		currentJobListTable.repaint();


		CompletedJobTableModel completedJobRenderer=
				(CompletedJobTableModel)completedJobListTable.getModel();

		if(completedJobListTable.getCellEditor() != null ) {
			completedJobListTable.getCellEditor().stopCellEditing();
			//solves cell updating bug
		}
		
		completedJobRenderer.addBatch(b);
		completedJobListTable.repaint();
		String msg="Order No. "+b.getBatchNumber()+" ID :"+b.getBatchId()+" completed";
		showNotification("Order Completed",msg, MessageType.INFO);

		completedJobsList.repaint();
	}

	public void addAcceptedJobToList(Batch order) {
		CurrentJobTableModel CurrJobListRenderer=(CurrentJobTableModel)currentJobListTable.getModel();
		
		if(currentJobListTable.getCellEditor() != null ) {
			currentJobListTable.getCellEditor().stopCellEditing();
			//solves cell updating bug
		}
		
		CurrJobListRenderer.addBatch(order);
		currentJobListTable.repaint();
		String msg="Order No. "+order.getBatchNumber()+" ID : "+order.getBatchId()+" accepted";
		showNotification("New Order",msg, MessageType.INFO);
		currentJobList.repaint();
	}

	public void addNegotiationBid(Batch jobUnderNegotiation) {
		NegotiationJobTileTableModel negotiationRenderer =
				(NegotiationJobTileTableModel)negotiationJobListTable.getModel();
		if(negotiationJobListTable.getCellEditor() != null ) {
			negotiationJobListTable.getCellEditor().stopCellEditing();
			//solves cell updating bug
		}
		negotiationRenderer.addBatch(jobUnderNegotiation);

		negotiationJobListTable.revalidate();
		negotiationJobListTable.repaint();

		String msg="Bid recieved for order no. "+jobUnderNegotiation.getBatchNumber()
				+" ID : "+jobUnderNegotiation.getBatchId();
		showNotification("New Bid", msg, MessageType.INFO);
	}

	public void cancelBatchUnderProcess(Batch batch){
		CurrentJobTableModel CurrjobListRenderer=
				(CurrentJobTableModel)currentJobListTable.getModel();
		CurrjobListRenderer.removeJob(batch);
		
		if(currentJobListTable.getCellEditor()!=null){
			currentJobListTable.getCellEditor().stopCellEditing();
			//solves cell updating bug
		}
		currentJobListTable.repaint();
		currentJobList.repaint();
	}

	public static void showNotification(String title, String message,TrayIcon.MessageType type){

		switch(type){
		case ERROR :
			GSAguiIcon.displayMessage(title,message, TrayIcon.MessageType.ERROR);
			break;

		case INFO:
			GSAguiIcon.displayMessage( title,message, TrayIcon.MessageType.INFO);
			break;

		case WARNING:
			GSAguiIcon.displayMessage( title,message, TrayIcon.MessageType.WARNING);
			break;

		case NONE:
			GSAguiIcon.displayMessage( title,message, TrayIcon.MessageType.NONE);
			break;

		}

		String notificationSound = "resources/notification.wav";
		InputStream in=null;
		try {
			in = new FileInputStream(notificationSound);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// create an audiostream from the inputstream
		AudioStream audioStream=null;
		try {
			audioStream = new AudioStream(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// play the audio clip with the audioplayer class
		AudioPlayer.player.start(audioStream);
	}

	public void clean() {
		tray.remove(GSAguiIcon);
		
	}

}
