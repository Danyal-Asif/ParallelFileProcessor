package com.parallelFileProcessor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUI {

    public static Path getInputPath(String s) {
        JFileChooser jd = s == null ? new JFileChooser() : new JFileChooser(s);
        jd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jd.setDialogTitle("Choose input file");
        int returnVal = jd.showOpenDialog(null);
        System.out.println(returnVal);
        if (returnVal != JFileChooser.APPROVE_OPTION) return null;
        return jd.getSelectedFile().toPath();
    }

    private static void showAndCreateGUI() {
        JFrame frame = new JFrame("ParallelFileProcessor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(380, 330);
        frame.setResizable(false);
        
        
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(1,1));
        
        JPanel chckboxPanel = new JPanel();
   
        frame.add(panel);
        Border border = BorderFactory.createTitledBorder("Filter");
        chckboxPanel.setBorder(border);
        
        JLabel label = new JLabel("Enter Directory: ");
        JTextField tf = new JTextField(15);
        JButton dir = new JButton("Dir");

        // ActionListener to the "Dir" button
        dir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Path selectedPath = getInputPath(tf.getText());
                tf.setText(selectedPath == null ? "" : selectedPath.toString());
            }
        }); 

        panel.add(label);
        panel.add(tf);
        panel.add(dir);
        
        JLabel numOfThreads=new JLabel("Number of Threads:             ");
        numOfThreads.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 14));
        JComboBox<Integer> c1=new JComboBox<Integer>(new Integer[]{1,2,3,4});
        c1.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 14));
        c1.setSelectedItem(4);
        c1.setPreferredSize(new Dimension(85,45));

        panel.add(numOfThreads);
        panel.add(c1);
        
        chckboxPanel.setLayout(new BoxLayout(chckboxPanel, BoxLayout.Y_AXIS));
        
        JCheckBox pdf=new JCheckBox("pdf");
        chckboxPanel.add(pdf);
        JCheckBox docx=new JCheckBox("docx");
        chckboxPanel.add(docx);
        JCheckBox txt=new JCheckBox("txt");
        chckboxPanel.add(txt);
        
        JPanel buttonPanel=new JPanel();
        
        JButton search=new JButton("Search");

        buttonPanel.add(search);
        
        search.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String,Boolean> filters=new HashMap<String,Boolean>();
		        filters.put("pdf", pdf.isSelected());
		        filters.put("docx", docx.isSelected());
		        filters.put("txt", txt.isSelected());
		        
		        final JDialog loading = new JDialog(frame);
		        JPanel p1 = new JPanel(new BorderLayout());
		        p1.add(new JLabel("Please wait..."), BorderLayout.CENTER);
		        loading.setUndecorated(true);
		        loading.getContentPane().add(p1);
		        loading.pack();
		        loading.setLocationRelativeTo(frame);
		        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		        loading.setModal(true);
		        
		        FileProcessor filep=new FileProcessor((int)c1.getSelectedItem(),filters);
		        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
		            @Override
		            protected String doInBackground() throws InterruptedException{
		            	filep.processFiles(tf.getText()!=""?tf.getText():"");
		            	
						return null; 
		                /** Execute some operation */   
		            }
		            @Override
		            protected void done() {
		                loading.dispose();
		            }
		        };
		        
		        worker.execute();
		        loading.setVisible(true);
		        
				List<String> processedFiles = filep.getProcessedFiles();
				List<String> processedMD5_code = filep.getProcessedMD5Codes();
				List<String> processedLinesCount=filep.getProcessedLinesCount();
				List<String> filePath=filep.getFilePath();
				JDialog tableDialog = new JDialog(frame, "Search Results", true);
                tableDialog.setSize(650, 320);
                tableDialog.setResizable(false);
                String [][] data= new String[processedFiles.size()][3];
                
                for (int i = 0; i < processedFiles.size(); i++) {
                    data[i][0] =processedFiles.get(i);  // Filename
                    data[i][1] = processedMD5_code.get(i);  // MD5
                    data[i][2] = processedLinesCount.get(i);  // Line Count
                }
                String[] columns= {"Filename","MD5","Line Count"};
                JTable table = new JTable(data, columns){
                	public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int rowIndex = rowAtPoint(p);
                        int colIndex = columnAtPoint(p);
                        int realColumnIndex = convertColumnIndexToModel(colIndex);

                        if (realColumnIndex == 0) { 
                            tip = filePath.get(rowIndex);

                        } else { 
                        }
                        return tip;
                    }
                };
                
                table.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent mouseEvent) {
                       JTable table =(JTable) mouseEvent.getSource();
                        Point point = mouseEvent.getPoint();
                        int row = table.rowAtPoint(point);
                        int col=table.columnAtPoint(point);
                        if(col==0) {
                        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                        	StringSelection stringSelection = new StringSelection(filePath.get(row));
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        }
                        }
                    }
                });
                
                JScrollPane sp = new JScrollPane(table);
                sp.setPreferredSize(new Dimension(600, 320));
                TableColumnModel columnModel = table.getColumnModel();
                table.setRowHeight(30);
                
                columnModel.getColumn(0).setPreferredWidth(170);
                columnModel.getColumn(1).setPreferredWidth(350);
                columnModel.getColumn(2).setPreferredWidth(80);
                
               
                
                table.setEnabled(true);                
                tableDialog.add(sp);
                tableDialog.setVisible(true);
                filters.clear();
               
				
			}
		});
        
        
        frame.add(chckboxPanel);
        frame.add(buttonPanel);
        frame.setLayout(new GridLayout(3,1,5,5));
        frame.setVisible(true);
    }

    public static void main(String[] args) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		public void run() {
    			showAndCreateGUI();
    		}
    	});
    }
}

