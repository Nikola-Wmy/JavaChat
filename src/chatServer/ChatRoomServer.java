package chatServer;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

class ClientList extends Thread{
	//�û��б����
	DatagramPacket p_mul;//�鲥����
	DatagramPacket pin;//���տͻ��˵ı���
	String str;//����ԭʼ�ַ���(��Э�����)
	String msg;//�û����͵���Ϣ����
	int port_private;//˽�Ķ�Ӧ��Ŀ�Ķ˿�
	InetAddress address_private;//˽�Ķ�Ӧ��Ŀ�ĵ�ַ
	MulticastSocket socket_mul;//�鲥socket
	DatagramSocket socket_private;//˽�ı���
	InetAddress ia_mul;//�����鲥��ַ
	InetAddress ia_private;//˽�ı��ص�ַ
	String type, name, value, list_send = null;//����Э�����/��Ϣ����
	Map<String, String> list = new HashMap<String,String>();//�洢�û��б�
	
	public void run() {
		try {
			socket_mul = new MulticastSocket(7000); 
			ia_private = InetAddress.getByName("127.0.0.1");
			socket_private = new DatagramSocket(9000,ia_private);
			ia_mul = InetAddress.getByName("225.0.1.2");
			socket_mul.joinGroup(ia_mul);
			while (true) {
				list_send = null;
				pin = new DatagramPacket(new byte[512], 512);
				socket_private.receive(pin);//�����û���Ϣ����ʽ("name|ip|port")
				str = new String(pin.getData(), 0, pin.getLength());
				String[] sou = str.split("\\|");//��str���л���,"\\"Ϊת���
				name = sou[0];
				value = sou[1];//��ʽ("ip|port")
				list.put(name, value);//���û����Լ����ڵ�ip��port�洢��map��
				//���û��б�����ַ�������ʽ("name";"name")
				for(String key:list.keySet()) {
					if(list_send==null) {
						list_send = key + ";";
					}else {
						list_send += key + ";";
					}
				}
				ChatRoomServer.items.add(name);//�޸��û��б�
				
				p_mul = new DatagramPacket(list_send.getBytes(), list_send.getBytes().length, ia_mul, 7000);
				socket_mul.send(p_mul);//�鲥�·������û��б�
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
class MsgServer extends Thread {
	DatagramPacket p_mul;//�鲥����
	DatagramPacket pin;//���տͻ��˵ı���
	String str;//����ԭʼ�ַ���(��Э�����)
	String msg;//�û����͵���Ϣ����
	int port_private;//˽�Ķ�Ӧ��Ŀ�Ķ˿�
	InetAddress address_private;//˽�Ķ�Ӧ��Ŀ�ĵ�ַ
	MulticastSocket socket_mul;//�鲥socket
	DatagramSocket socket_private;//˽�ı���
	InetAddress ia_mul;//�����鲥��ַ
	InetAddress ia_private;//˽�ı��ص�ַ
	String type = null;//����Э�����/��Ϣ����
	public void run() {
		try {
			socket_mul = new MulticastSocket(4000); 
			ia_private = InetAddress.getByName("127.0.0.1");
			socket_private = new DatagramSocket(8000,ia_private);
			ia_mul = InetAddress.getByName("225.0.1.1");
			socket_mul.joinGroup(ia_mul);
			while (true) {
				pin = new DatagramPacket(new byte[512], 512);
				socket_private.receive(pin);
				str = new String(pin.getData(), 0, pin.getLength());
				String[] sou = str.split("\\|");//��str���л���,"\\"Ϊת���
				type = sou[0];//
				msg = sou[1];//
				if(type.equals("1")){//����Ϊ1�����鲥
					multicastServer();
				}else if(type.equals("2")){//����Ϊ2����˽��
					address_private = InetAddress.getByName(sou[2]);
					port_private = Integer.parseInt(sou[3]);
					privateServer(address_private, port_private);
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public void multicastServer() {
		//�����鲥��Ϣ
		try {
			p_mul = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ia_mul, 4000);
			socket_mul.send(p_mul);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void privateServer(InetAddress address,int port) {
		//����˽����Ϣ
		try {
			DatagramPacket pout = new DatagramPacket(msg.getBytes(), msg.getBytes().length,address,
					port);
			socket_private.send(pout);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class ChatRoomServer extends Application {
	TextArea ta_Speaking;//�����
	GridPane ChatRoom;//���������
	BorderPane IPpane;
	BorderPane box;//ƴ�Ͻ���
	ListView<String> IPlist; //��ʾ�����û�
	TitledPane pane_ip,pane_send;
	static ObservableList<String> items;//�û���������
	MsgServer msgServer = new MsgServer();//������Ϣ�����߳�
	ClientList clientlist = new ClientList();
	@Override
	public void start(Stage primaryStage) throws Exception {
		//�����������
		ChatRoom = new GridPane();//�������
		ChatRoom.setHgap(10);//���ÿ���
		ChatRoom.setVgap(10);//���ø߼��
		
		ta_Speaking = new TextArea("");//�����
		ta_Speaking.setPrefHeight(600);//�����߶�
		
		ChatRoom.add(ta_Speaking,0,0);//�������0��0��
		ChatRoom.setPrefSize(655, 561);//��������750��531
		
		//�����û���������
		IPpane = new BorderPane();//�����û���������
		IPlist = new ListView<>();//�û������б�
		items =FXCollections.observableArrayList ();//�û���������
		IPlist.setItems(items);//��ʾ����
	    IPpane.setLeft(IPlist);//�б���ʾ�ڽ������
	    IPpane.setPrefWidth(100);//�û��б������
	    IPpane.setPrefHeight(561);//�û��б����߶�
	    
		
		//ƴ�Ͻ��� 
		pane_ip = new TitledPane("OnLine",IPpane);//�û��б������
		pane_ip.setCollapsible(false);//�շŹ���Ϊ��
		
		pane_send = new TitledPane("Chat",ChatRoom);//���������
		pane_send.setCollapsible(false);//�շŹ���Ϊ��
		
		box = new BorderPane();//һ��װ��������ĺ���
		box.setLeft(pane_ip);//������û��б�
		box.setRight(pane_send);//�ұ��������
		Scene scene = new Scene(box);
		primaryStage.setScene(scene);
		primaryStage.setTitle("ChatServer");//�ܽ�����
		primaryStage.show();//��ʾ����
	
		msgServer.start();
		clientlist.start();
	}
	
	
	public static void main(String[] args) {
		
		 Application.launch();
	}
}
