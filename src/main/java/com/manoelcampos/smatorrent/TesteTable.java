package com.manoelcampos.smatorrent;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import java.io.*;


public class TesteTable extends JFrame {
	private static final long serialVersionUID = 6080074094277623981L;

	public TesteTable() {
		super("Teste JTable");
		
		String[] columns = new String []{"File name","Size", "Status"};
		String[][] data = new String [][] {  
				{"Frejat-Segredos","39", "Idle"},  
				{"Foo Fighters-Best of You","42", "Seeding"},  
				{"Goo goo dolls-Iris","34", "Downloading"},  
				{"Frejat-Sobre nós dois e o resto do mundo","45", "Paused"}  
				};
		  
		// Ao inves de passar direto, colocamos os dados em um modelo  
		DefaultTableModel model = new DefaultTableModel(data, columns);  
		// e passamos o modelo para criar a jtable  
		JTable tb = new JTable(model);
		Container cp = this.getContentPane();
		cp.setLayout(new FlowLayout());
		this.setSize(600, 400);
		tb.setSize(this.getWidth()-30, this.getHeight()-80);

		cp.add(tb);
		add(new JScrollPane(tb));
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	//http://www.devmedia.com.br/articles/viewcomp.asp?comp=3245
	//http://www.devx.com/xml/Article/16921/0/
	public TesteTable(String xmlFileName) {
		try {
			XmlTorrentDataModel model = new XmlTorrentDataModel(xmlFileName);
			JTable tb = new JTable(model);
			Container cp = this.getContentPane();
			cp.setLayout(new FlowLayout());
			this.setSize(600, 400);
			tb.setSize(this.getWidth()-30, this.getHeight()-80);
			
			cp.add(tb);
			add(new JScrollPane(tb));
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void dataModel(String xmlFileName) {
		try {
		  XmlTorrentDataModel dm = new XmlTorrentDataModel(xmlFileName);
		  System.out.println("Linhas do XML: " + dm.getRowCount());
		  for(int j=0; j < dm.getColumnCount(); j++)
			  System.out.print(dm.getColumnName(j) + "   |");
		  System.out.println(); 
		  
		  for(int i=0; i < dm.getRowCount(); i++) {
		    for(int j=0; j < dm.getColumnCount(); j++)
		       System.out.print(dm.getValueAt(i, j) + "   |");
		    System.out.println();
		  }
		}catch(IOException e) {
			System.out.println(e.getLocalizedMessage());
		}catch(ParserConfigurationException e) {
			System.out.println(e.getLocalizedMessage());
		}catch(SAXException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	public static void main(String[] args) {
		String xmlFileName = "/home/manoel/workspace/iTorrent/src/com/manoelcampos/itorrent/iTorrent.xml";
		dataModel(xmlFileName);
		new TesteTable(xmlFileName);
		//new TesteTable();
	}
}
