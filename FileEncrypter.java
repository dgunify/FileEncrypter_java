import javax.swing.JFrame;
import java.awt.*; 
import java.awt.event.*; 
import javax.swing.*; 
import java.io.*; 
import java.security.*; 
import javax.crypto.*; 
import javax.crypto.spec.*; 
/** 
文件名：FileEncrypter.java 
JDK：1.40以上 
说明：文件加密 
加密方法：三重DES加密 
加密过程：对选中的文件加密后在同文件夹下生成一个增加了".zdg" 
扩展名的加密文件 
解密过程：对选中的加密文件（必须有".zdg"扩展名）进行解密 
*/ 
public class FileEncrypter extends JFrame{
	public static final int WIDTH = 550; 
	public static final int HEIGHT = 200; 

	public static void main(String args[]) { 
	FileEncrypter fe = new FileEncrypter(); 
	fe.show(); 
	} 

	FileEncrypter(){ 
	this.setSize(WIDTH,HEIGHT); 
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	this.setResizable(false); 
	Toolkit tk = Toolkit.getDefaultToolkit(); 
	Dimension screenSize = tk.getScreenSize(); 
	this.setLocation((screenSize.width - WIDTH)/2, 
	(screenSize.height - HEIGHT)/2); 
	this.setTitle("文件加密器(DeGuiKeJi)"); 
	Container c = this.getContentPane(); 
	c.setLayout( new FlowLayout()); 

	final FilePanel fp = new FilePanel("文件选择"); 
	c.add(fp); 

	final KeyPanel pp = new KeyPanel("密码"); 
	c.add(pp); 

	JButton jbE = new JButton("加密"); 
	c.add(jbE); 
	jbE.addActionListener(new ActionListener(){ 
	public void actionPerformed(ActionEvent event){ 
	File file = new File(fp.getFileName()); 
	if (file.exists()) 
	encrypt(file.getAbsoluteFile(),pp.getKey()); 
	else 
	JOptionPane.showMessageDialog( 
	null,"请选择文件！","提示",JOptionPane.OK_OPTION); 
	} 
	}); 
	JButton jbD = new JButton("解密"); 
	c.add(jbD); 
	jbD.addActionListener(new ActionListener(){ 
	public void actionPerformed(ActionEvent event){ 
	File file = new File(fp.getFileName()); 
	if (file.exists()) 
	decrypt(file.getAbsoluteFile(),pp.getKey()); 
	else 
	JOptionPane.showMessageDialog( 
	null,"请选择文件！","提示",JOptionPane.OK_OPTION); 
	} 
	}); 
	} 

	/** 
	加密函数 
	输入： 
	要加密的文件，密码（由0-F组成，共48个字符，表示3个8位的密码）如： 
	AD67EA2F3BE6E5ADD368DFE03120B5DF92A8FD8FEC2F0746 
	其中： 
	AD67EA2F3BE6E5AD DES密码一 
	D368DFE03120B5DF DES密码二 
	92A8FD8FEC2F0746 DES密码三 
	输出： 
	对输入的文件加密后，保存到同一文件夹下增加了".zdg"扩展名的文件中。 
	*/ 
	private void encrypt(File fileIn,String sKey){
	try{ 
	if(sKey.length() == 48){ 
	byte[] bytK1 = getKeyByStr(sKey.substring(0,16)); 
	byte[] bytK2 = getKeyByStr(sKey.substring(16,32)); 
	byte[] bytK3 = getKeyByStr(sKey.substring(32,48)); 

	FileInputStream fis = new FileInputStream(fileIn); 
	byte[] bytIn = new byte[(int)fileIn.length()]; 
	for(int i = 0;i<fileIn.length();i++){ 
	bytIn[i] = (byte)fis.read(); 
	} 
	//加密 
	byte[] bytOut = encryptByDES(encryptByDES( 
	encryptByDES(bytIn,bytK1),bytK2),bytK3); 
	String fileOut = fileIn.getPath() + ".zdg"; 
	FileOutputStream fos = new FileOutputStream(fileOut); 
	for(int i = 0;i<bytOut.length;i++){ 
	fos.write((int)bytOut[i]); 
	} 
	fos.close(); 
	JOptionPane.showMessageDialog( 
	this,"加密成功！","提示",JOptionPane.OK_OPTION); 
	}else 
	JOptionPane.showMessageDialog( 
	this,"密码长度必须等于48！","错误信息",JOptionPane.ERROR_MESSAGE); 
	}catch(Exception e){ 
	e.printStackTrace(); 
	} 
	} 

	/** 
	解密函数 
	输入： 
	要解密的文件，密码（由0-F组成，共48个字符，表示3个8位的密码）如： 
	AD67EA2F3BE6E5ADD368DFE03120B5DF92A8FD8FEC2F0746 
	其中： 
	AD67EA2F3BE6E5AD DES密码一 
	D368DFE03120B5DF DES密码二 
	92A8FD8FEC2F0746 DES密码三 
	输出： 
	对输入的文件解密后，保存到用户指定的文件中。 
	*/ 
	private void decrypt(File fileIn,String sKey){ 
	try{ 
	if(sKey.length() == 48){ 
	String strPath = fileIn.getPath(); 
	int hzIndex = strPath.length()-4;
	String hzStr = strPath.substring(hzIndex);
	if(hzStr.toLowerCase().equals(".zdg")) 
	strPath = strPath.substring(0,strPath.length()-4); 
	else{ 
	JOptionPane.showMessageDialog( 
	this,"不是合法的加密文件！","提示",JOptionPane.OK_OPTION); 
	return; 
	} 
	JFileChooser chooser = new JFileChooser(); 
	chooser.setCurrentDirectory(new File(".")); 
	chooser.setSelectedFile(new File(strPath)); 
	//用户指定要保存的文件 
	int ret = chooser.showSaveDialog(this); 
	if(ret==JFileChooser.APPROVE_OPTION){ 

	byte[] bytK1 = getKeyByStr(sKey.substring(0,16)); 
	byte[] bytK2 = getKeyByStr(sKey.substring(16,32)); 
	byte[] bytK3 = getKeyByStr(sKey.substring(32,48)); 

	FileInputStream fis = new FileInputStream(fileIn); 
	byte[] bytIn = new byte[(int)fileIn.length()]; 
	for(int i = 0;i<fileIn.length();i++){ 
	bytIn[i] = (byte)fis.read(); 
	} 
	//解密 
	byte[] bytOut = decryptByDES(decryptByDES( 
	decryptByDES(bytIn,bytK3),bytK2),bytK1); 
	File fileOut = chooser.getSelectedFile(); 
	fileOut.createNewFile(); 
	FileOutputStream fos = new FileOutputStream(fileOut); 
	for(int i = 0;i<bytOut.length;i++){
	fos.write((int)bytOut[i]); 
	} 
	fos.close(); 
	JOptionPane.showMessageDialog( 
	this,"解密成功！","提示",JOptionPane.OK_OPTION); 
	} 
	}else 
	JOptionPane.showMessageDialog( 
	this,"密码长度必须等于48！","错误信息",JOptionPane.ERROR_MESSAGE); 
	}catch(Exception e){ 
	JOptionPane.showMessageDialog( 
	this,"解密失败，请核对密码！","提示",JOptionPane.OK_OPTION); 
	} 
	} 

	/** 
	用DES方法加密输入的字节 
	bytKey需为8字节长，是加密的密码 
	*/ 
	private byte[] encryptByDES(byte[] bytP,byte[] bytKey) throws Exception{ 
	DESKeySpec desKS = new DESKeySpec(bytKey); 
	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES"); 
	SecretKey sk = skf.generateSecret(desKS); 
	Cipher cip = Cipher.getInstance("DES"); 
	cip.init(Cipher.ENCRYPT_MODE,sk); 
	return cip.doFinal(bytP); 
	} 

	/** 
	用DES方法解密输入的字节 
	bytKey需为8字节长，是解密的密码 
	*/ 
	private byte[] decryptByDES(byte[] bytE,byte[] bytKey) throws Exception{ 
	DESKeySpec desKS = new DESKeySpec(bytKey); 
	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES"); 
	SecretKey sk = skf.generateSecret(desKS); 
	Cipher cip = Cipher.getInstance("DES"); 
	cip.init(Cipher.DECRYPT_MODE,sk); 
	return cip.doFinal(bytE); 
	} 

	/** 
	输入密码的字符形式，返回字节数组形式。 
	如输入字符串：AD67EA2F3BE6E5AD 
	返回字节数组：{173,103,234,47,59,230,229,173} 
	*/ 
	private byte[] getKeyByStr(String str){ 
	byte[] bRet = new byte[str.length()/2];
		for(int i=0;i<str.length();i++){
			if(bRet[bRet.length-1]==0){
			Integer itg = new Integer(16*getChrInt(str.charAt(2*i)) + getChrInt(str.charAt(2*i+1))); 
			bRet[i] = itg.byteValue(); 
			}
	}
	return bRet; 
	} 
	/** 
	计算一个16进制字符的10进制值 
	输入：0-F 
	*/ 
	private int getChrInt(char chr){ 
	int iRet=0; 
	if(chr=="0".charAt(0)) iRet = 0; 
	if(chr=="1".charAt(0)) iRet = 1; 
	if(chr=="2".charAt(0)) iRet = 2; 
	if(chr=="3".charAt(0)) iRet = 3; 
	if(chr=="4".charAt(0)) iRet = 4; 
	if(chr=="5".charAt(0)) iRet = 5; 
	if(chr=="6".charAt(0)) iRet = 6; 
	if(chr=="7".charAt(0)) iRet = 7; 
	if(chr=="8".charAt(0)) iRet = 8; 
	if(chr=="9".charAt(0)) iRet = 9; 
	if(chr=="A".charAt(0)) iRet = 10; 
	if(chr=="B".charAt(0)) iRet = 11; 
	if(chr=="C".charAt(0)) iRet = 12; 
	if(chr=="D".charAt(0)) iRet = 13; 
	if(chr=="E".charAt(0)) iRet = 14; 
	if(chr=="F".charAt(0)) iRet = 15; 
	return iRet; 
	} 
	} 

	/** 
	文件选择组件。 
	*/ 
	class FilePanel extends JPanel{ 
	FilePanel(String str){ 
	JLabel label = new JLabel(str); 
	JTextField fileText = new JTextField(35); 
	JButton chooseButton = new JButton("浏览..."); 
	this.add(label); 
	this.add(fileText); 
	this.add(chooseButton); 
	clickAction ca = new clickAction(this); 
	chooseButton.addActionListener(ca); 

	} 

	public String getFileName(){ 
	JTextField jtf = (JTextField)this.getComponent(1); 
	return jtf.getText(); 
	} 

	private class clickAction implements ActionListener{ 
	clickAction(Component c){ 
	cmpt = c; 
	} 

	public void actionPerformed(ActionEvent event){ 
	JFileChooser chooser = new JFileChooser(); 
	chooser.setCurrentDirectory(new File(".")); 
	int ret = chooser.showOpenDialog(cmpt); 
	if(ret==JFileChooser.APPROVE_OPTION){ 
	JPanel jp = (JPanel)cmpt; 
	JTextField jtf = (JTextField)jp.getComponent(1); 
	jtf.setText(chooser.getSelectedFile().getPath()); 
	} 
	} 

	private Component cmpt; 
	} 
	} 

	/** 
	密码生成组件。 
	*/ 
	class KeyPanel extends JPanel{ 
	KeyPanel(String str){ 
	JLabel label = new JLabel(str); 
	JTextField fileText = new JTextField(35); 
	JButton chooseButton = new JButton("随机产生"); 
	this.add(label); 
	this.add(fileText); 
	this.add(chooseButton); 
	clickAction ca = new clickAction(this); 
	chooseButton.addActionListener(ca); 

	} 

	//返回生成的密码（48个字符长度） 
	public String getKey(){ 
	JTextField jtf = (JTextField)this.getComponent(1); 
	return jtf.getText(); 
	} 

	private class clickAction implements ActionListener{ 
	clickAction(Component c){ 
	cmpt = c; 
	} 

	public void actionPerformed(ActionEvent event){ 
		try{ 
			KeyGenerator kg = KeyGenerator.getInstance("DES"); 
			kg.init(56); 
			Key ke = kg.generateKey(); 
			byte[] bytK1 = ke.getEncoded(); 
			ke = kg.generateKey(); 
			byte[] bytK2 = ke.getEncoded(); 
			ke = kg.generateKey(); 
			byte[] bytK3 = ke.getEncoded(); 
		
			JPanel jp = (JPanel)cmpt; 
			JTextField jtf = (JTextField)jp.getComponent(1); 
			jtf.setText(getByteStr(bytK1)+getByteStr(bytK2)+getByteStr(bytK3)); 
			}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 

	private String getByteStr(byte[] byt){ 
	String strRet = ""; 
	for(int i=0;i<byt.length;i++){ 
	//System.out.println(byt[i]); 
	strRet += getHexValue((byt[i]&240)/16); 
	strRet += getHexValue(byt[i]&15); 
	} 
	return strRet; 
	} 

	private String getHexValue(int s){ 
	String sRet=null; 
	switch (s){ 
	case 0: sRet = "0";break; 
	case 1: sRet = "1";break; 
	case 2: sRet = "2";break; 
	case 3: sRet = "3";break; 
	case 4: sRet = "4";break; 
	case 5: sRet = "5";break; 
	case 6: sRet = "6";break; 
	case 7: sRet = "7";break; 
	case 8: sRet = "8";break; 
	case 9: sRet = "9";break; 
	case 10: sRet = "A";break; 
	case 11: sRet = "B";break; 
	case 12: sRet = "C";break; 
	case 13: sRet = "D";break; 
	case 14: sRet = "E";break; 
	case 15: sRet = "F"; 
	} 
	return sRet; 
	} 

	private Component cmpt; 
	} 
}
