package chat;

import Model.DataSocket;
import Model.User;
import UI.app_main_ui;
import UI.item_user_list;
import UI.one_two_three;
import UI.result_ui;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

public class Receive extends Thread {

    Socket socket = null;
    ObjectInputStream inputStream = null;
    DataSocket dataSocket = null;
    Clip clip;

    public Receive(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            inputStream = new ObjectInputStream(this.socket.getInputStream());
            while (true) {
                dataSocket = (DataSocket) inputStream.readObject();
                System.out.println(dataSocket.action);
                switch (dataSocket.action) {
                    case "loginok":
                        loadUser();
                        break;
                    case "loaduser":
                        showUser(dataSocket.getUserList());
                        break;
                    case "respon_call":
                        responCall(dataSocket);
                        break;
                    case "request_call":
                        requestCall(dataSocket);
                        break;
                    case "sendfinger":
                        receiveFinger(dataSocket);
                        break;
                    case "request_phone":
                    	requestPhone(dataSocket);
                    case "response_phone":
                    	responsePhone(dataSocket);
                    case "end_phone":
                    	endPhone(dataSocket);
                    default:
                        System.out.println("Unknow action");
                }
            }
        } catch (IOException ex) {
            //Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void requestCall(DataSocket data) {
        System.out.println("???? nh???n y??u c???u ch??i t??? " + data.data[0]);

        if (ChatClient.playing) { // client ??ang ch??i
            DataSocket dtsk = new DataSocket();
            dtsk.action = "respon_call";
            String[] data2 = new String[3];
            data2[0] = String.valueOf(ChatClient.myUserName);
            data2[1] = String.valueOf(data.data[0]); //Username ng?????i m???i
            data2[2] = "??ang b???n ch??i";
            dtsk.data = data2;
            dtsk.accept = false;
            ObjectOutputStream dout;
            try {
                dout = new ObjectOutputStream(ChatClient.socket.getOutputStream());
                dout.writeObject(dtsk);
                System.out.println("???? g???i ph???n h???i: t??? ch???i");
            } catch (IOException ex) {
                Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ChatClient.myRivalUserName = data.data[0];
            ChatClient.app_main_ui.showcallnotify(data.data[0]);
            ChatClient.app_main_ui.data_from_server = data.data;
            ChatClient.playing = true;
        }
    }

    private void responCall(DataSocket data) {
        System.out.println(data.data[0] + " ???? ph???n h???i y??u c???u");

        // T??? ch???i
        if (!data.accept) {
            if(ChatClient.inviteUI != null){
                try {
                    ChatClient.inviteUI.lb_status.setText("???? t??? ch???i");
                    Thread.sleep(2000);
                    ChatClient.inviteUI.setVisible(false);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ChatClient.myRivalUserName = "";
            ChatClient.playing = false;
        // ?????ng ??    
        } else {
            ChatClient.inviteUI.setVisible(false);
            if(ChatClient.OTT != null){
                ChatClient.OTT.setVisible(true);
                ChatClient.OTT.startCount();
            }
        }
    }

    private void loadUser() throws IOException {
        ChatClient.login = true;

        ChatClient.app_main_ui.setVisible(true);
        ChatClient.app_main_ui.setTitle("O???n t?? t??");
        ChatClient.login_ui.setVisible(false);

        OutputStream outputStream = ChatClient.socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        DataSocket dtsk = new DataSocket();
        dtsk.action = "loaduser";
        objectOutputStream.writeObject(dtsk);
    }

    private void showUser(List<User> data) {
        ChatClient.userList.clear();
        DefaultListModel<User> defaultListModel = new DefaultListModel<>();
        
        for(User user : data){
            if(!user.getUserName().equals(ChatClient.myUserName)){
                ChatClient.userList.add(user);
                defaultListModel.addElement(user);
            }
        }

        app_main_ui.list_user.setModel(defaultListModel);
        app_main_ui.list_user.setCellRenderer(new item_user_list());
    }
    
    private void receiveFinger(DataSocket data){
        int rivalFinger = Integer.parseInt(data.data[2]);
        int myFinger = ChatClient.OTT.fingerValue;
        
        switch(myFinger){
            case 1:
                switch(rivalFinger){
                    case 1: hoa(); break;
                    case 2: case 4: case 5: thang(); break;
                    case 3: thua(); break;
                }
                break;
            case 2:
                switch(rivalFinger){
                    case 1: thua(); break;
                    case 3: case 4: case 5: thang(); break;
                    case 2: hoa(); break;
                }
                break; 
            case 3:
                switch(rivalFinger){
                    case 1: case 4: case 5: thang(); break;
                    case 2: thua(); break;
                    case 3: hoa(); break;
                }
                break;
            case 4:
                switch(rivalFinger){
                    case 1: case 2: case 3: thua(); break;
                    case 4: case 5: hoa(); break;
                }
                break;
            case 5:
                switch(rivalFinger){
                    case 1: case 2: case 3: thua(); break;
                    case 4: case 5: hoa(); break;
                }
                break;      
        }
    }
    
    private void thang(){
        result_ui resultUI = new result_ui();
        resultUI.txt_ketqua.setText("B???n th???ng!");
        resultUI.img_result.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("image/win.jpg")).getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT)));
        resultUI.setVisible(true);
    }
    
    private void thua(){
        result_ui resultUI = new result_ui();
        resultUI.txt_ketqua.setText("B???n thua!");
        resultUI.img_result.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("image/lost.jpg")).getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT)));
        resultUI.setVisible(true);
    }
    
    private void hoa(){
        result_ui resultUI = new result_ui();
        resultUI.txt_ketqua.setText("B???n h??a!");
        resultUI.img_result.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("image/hoa.jpg")).getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT)));
        resultUI.setVisible(true);
    }

    private void requestPhone(DataSocket data) {
        System.out.println("???? nh???n y??u c???u cu???c g???i t??? " + data.data[0]);

        if (ChatClient.playing) { // client ??ang ch??i
            DataSocket dtsk = new DataSocket();
            dtsk.action = "respon_call";
            String[] data2 = new String[3];
            data2[0] = String.valueOf(ChatClient.myUserName);
            data2[1] = String.valueOf(data.data[0]); //Username ng?????i m???i
            data2[2] = "??ang b???n ch??i";
            dtsk.data = data2;
            dtsk.accept = false;
            ObjectOutputStream dout;
            try {
                dout = new ObjectOutputStream(ChatClient.socket.getOutputStream());
                dout.writeObject(dtsk);
                System.out.println("???? g???i ph???n h???i: t??? ch???i");
            } catch (IOException ex) {
                Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ChatClient.myRivalUserName = data.data[0];
            ChatClient.app_main_ui.showcallnotify(data.data[0]);
            ChatClient.app_main_ui.data_from_server = data.data;
            ChatClient.playing = true;
        }
    }

    private void responsePhone(DataSocket data) {
        System.out.println(data.data[0] + " ???? ph???n h???i y??u c???u cu???c g???i");

        // T??? ch???i
        if (!data.accept) {
            if(ChatClient.inviteUI != null){
                try {
                    ChatClient.inviteUI.lb_status.setText("???? t??? ch???i");
                    Thread.sleep(2000);
                    ChatClient.inviteUI.setVisible(false);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ChatClient.myRivalUserName = "";
            ChatClient.playing = false;
        // ?????ng ??    
        } else {
            ChatClient.inviteUI.setVisible(false);
            if(ChatClient.OTT != null){
                ChatClient.OTT.setVisible(true);
                ChatClient.OTT.startCount();
            }
        }
    }

    private void endPhone(DataSocket respon) {
        
    }
}
