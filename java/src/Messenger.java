/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
 

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   public static String sanitize_input(String s){
     String ret = s.replace("'", "''");
     return ret;
   }

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
			System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
				System.out.println();
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Contacts");
                System.out.println("2. Update Status Message");
                System.out.println("3. Chats");
                System.out.println("4. Delete account");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Contacts(esql, authorisedUser); break;
                   case 2: UpdateStatusMessage(esql, authorisedUser); break;
                   case 3: Chats(esql, authorisedUser); break;
                   case 4: authorisedUser = DeleteAccount(esql, authorisedUser); usermenu = authorizedUser != null; break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(sanitize_input(in.readLine()));
			 System.out.println();
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = sanitize_input(in.readLine());
         System.out.print("\tEnter user password: ");
         String password = sanitize_input(in.readLine());
         System.out.print("\tEnter user phone: ");
         String phone = sanitize_input(in.readLine());

	 //Creating empty contact\block lists for a user
	 esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
	 int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
	 int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
	 String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
        System.out.println("Your username or phone number is already in use");
         //System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = sanitize_input(in.readLine());
         System.out.print("\tEnter user password: ");
         String password = sanitize_input(in.readLine());

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
	 else
		 System.err.println("\tInvalid Credentials.");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   //--------------------------------------------------------
   //                 CONTACTS
   //--------------------------------------------------------

   public static void Contacts(Messenger esql, String user){
	  boolean contactsmenu = true;
	  while(contactsmenu) {
		System.out.println();
		System.out.println("CONTACTS MENU");
		System.out.println("---------");
		System.out.println("1. List Contacts");
		System.out.println("2. Add Contact");
		System.out.println("3. Delete Contact");
		System.out.println("4. List Blocked Contacts");
		System.out.println("5. Block Contact");
		System.out.println("6. Unblock Contact");
		System.out.println(".........................");
		System.out.println("9. Back");
		switch (readChoice()){
		   case 1: ListContacts(esql, user); break;
		   case 2: AddToContacts(esql, user); break;
		   case 3: DeleteFromContacts(esql, user); break;
		   case 4: ListBlockedContacts(esql, user); break;
		   case 5: BlockContact(esql, user); break;
		   case 6: UnblockContact(esql, user); break;
		   case 9: contactsmenu = false; break;
		   default : System.out.println("Unrecognized choice!"); break;
		}
	  }
   }

   public static void ListContacts(Messenger esql, String user){
	try{   
         String query = String.format("SELECT list_member, U2.status FROM User_list_contains L, Usr U, Usr U2 WHERE U.login = '%s' AND L.list_id = U.contact_list AND U2.login=L.list_member", user);
		 int rows = esql.executeQueryAndPrintResult(query);

		 if(rows == 0)
			 System.out.println("No contacts");

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void AddToContacts(Messenger esql, String user){
	   try{
		 System.out.print("\tEnter contact to add: ");
         String contact = sanitize_input(in.readLine());
		 String query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
         int userNum = esql.executeQuery(query);
		 if (userNum > 0 && user.compareTo(contact) != 0){

		   query = String.format("SELECT contact_list FROM usr WHERE login = '%s'", user);
		   String list_id = esql.executeQueryAndReturnResult(query).get(0).get(0);


		   query = String.format("SELECT * FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		   userNum = esql.executeQuery(query);
		   if(userNum > 0){
			   System.err.println("\tContact already exists in contact list");
			   return;
		   }

		   query = String.format("INSERT INTO user_list_contains(list_id, list_member) VALUES('%s', '%s') ", list_id, contact );
		   esql.executeUpdate(query);

		   System.out.println("\tContact added successfully.");

		 }
		 else{
			 if(user.compareTo(contact) == 0)
				 System.err.println("\tCannot add yourself to your contact list");
			 else
				 System.err.println("\tUser does not exist.");
		 }
	   } catch(Exception e){
		   System.err.println(e.getMessage());
	   }

   }//end

   public static void DeleteFromContacts(Messenger esql, String user){
	   try{
		 System.out.print("\tEnter contact to delete: ");
         String contact = sanitize_input(in.readLine());
	
	     String query = String.format("SELECT contact_list FROM usr WHERE login = '%s'", user);
	     String list_id = esql.executeQueryAndReturnResult(query).get(0).get(0);
		 query = String.format("SELECT * FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		 int userNum = esql.executeQuery(query);
		 if (userNum > 0){

		   query = String.format("DELETE FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		   esql.executeUpdate(query);

		   System.out.println("\tContact deleted successfully.");

		 }
		 else{
			 System.err.println("\tUser does not exist in contact list.");
		 }
	   } catch(Exception e){
		   System.err.println(e.getMessage());
	   }

   }//end

   public static void ListBlockedContacts(Messenger esql, String user){
	try{   
         String query = String.format("SELECT list_member, U2.status FROM User_list_contains L, Usr U,Usr U2 WHERE U.login = '%s' AND L.list_id = U.block_list AND U2.login=L.list_member", user);
		 int rows = esql.executeQueryAndPrintResult(query);

		 if(rows == 0)
			 System.out.println("No blocked contacts");

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }//end

   public static void BlockContact(Messenger esql, String user){
	   try{
		 System.out.print("\tEnter contact to block: ");
         String contact = sanitize_input(in.readLine());
		 String query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
         int userNum = esql.executeQuery(query);
		 if (userNum > 0 && user.compareTo(contact) != 0){

		   query = String.format("SELECT block_list FROM usr WHERE login = '%s'", user);
		   String list_id = esql.executeQueryAndReturnResult(query).get(0).get(0);

		   query = String.format("SELECT * FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		   userNum = esql.executeQuery(query);
		   if(userNum > 0){
			   System.err.println("\tContact already exists in block list");
			   return;
		   }

		   query = String.format("INSERT INTO user_list_contains(list_id, list_member) VALUES('%s', '%s') ", list_id, contact );
		   esql.executeUpdate(query);

		   System.out.println("\tContact blocked successfully.");

		 }
		 else{
			 if(user.compareTo(contact) == 0)
				 System.err.println("\tCannot block yourself");
			 else
				 System.err.println("\tUser does not exist.");
		 }
	   } catch(Exception e){
		   System.err.println(e.getMessage());
	   }

   }//end

   public static void UnblockContact(Messenger esql, String user){
	   try{
		 System.out.print("\tEnter contact to unblock: ");
         String contact = sanitize_input(in.readLine());
	
	     String query = String.format("SELECT block_list FROM usr WHERE login = '%s'", user);
	     String list_id = esql.executeQueryAndReturnResult(query).get(0).get(0);
		 query = String.format("SELECT * FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		 int userNum = esql.executeQuery(query);
		 if (userNum > 0){

		   query = String.format("DELETE FROM user_list_contains WHERE list_id = '%s' AND list_member = '%s'", list_id, contact );
		   esql.executeUpdate(query);

		   System.out.println("\tContact unblocked successfully.");

		 }
		 else{
			 System.err.println("\tUser does not exist in block list.");
		 }
	   } catch(Exception e){
		   System.err.println(e.getMessage());
	   }   
   }//end
   //--------------------------------------------------------
   //                UPDATE STATUS MESSAGE
   //--------------------------------------------------------

   public static void UpdateStatusMessage(Messenger esql, String user){
	   try{
		   System.out.println("Your current status message is:");
		   String query = String.format("SELECT status FROM usr WHERE login='%s'", user);
		   String status = esql.executeQueryAndReturnResult(query).get(0).get(0);
		   System.out.println(status);
	   System.out.println("New status message: ");
	   String newstatus = sanitize_input(in.readLine());
		while(true){
			System.out.print("\tupdate (u) or cancel(c)? ");
			String input = sanitize_input(in.readLine());
			if(input.compareToIgnoreCase("cancel")== 0 || input.compareToIgnoreCase("c") == 0){
				System.out.println("Status not updated.");
				break;
			}
			else if(input.compareToIgnoreCase("update")== 0 || input.compareToIgnoreCase("u") == 0){
				//send message
				query = String.format("UPDATE usr SET status='%s' WHERE login='%s'", newstatus, user);
				esql.executeUpdate(query);
				System.out.println("Status updated.");
				break;
			}
			else
				System.err.println("\tUnrecognized command!");
		}


	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }
	   
   }

   //--------------------------------------------------------
   //                 CHATS
   //--------------------------------------------------------


   public static void Chats(Messenger esql, String user){
	  boolean chatsmenu = true;
	  while(chatsmenu) {
		System.out.println();
		System.out.println("CHATS MENU");
		System.out.println("---------");
		System.out.println("1. List Chats");
		System.out.println("2. New Chat");
		System.out.println("3. View Chat");
		System.out.println("4. Edit Chat");
		System.out.println(".........................");
		System.out.println("9. Back");
		switch (readChoice()){
		   case 1: ListChats(esql, user); break;
		   case 2: NewChat(esql, user); break;
		   case 3: ViewChat(esql, user); break;
		   case 4: EditChat(esql, user); break;
		   case 9: chatsmenu = false; break;
		   default : System.out.println("Unrecognized choice!"); break;
		}
	  }
   }

   public static void ListChats(Messenger esql, String user){
	try{   
		 
        // String query = String.format("SELECT chat.chat_id, chat_type, init_sender, msg_timestamp FROM chat, chat_list, message WHERE member = '%s' AND chat.chat_id = chat_list.chat_id AND chat.chat_id = message.chat_id ORDER BY msg_timestamp", user);
		String query = String.format("SELECT chat.chat_id, chat_type, init_sender, max AS msg_timestamp FROM chat, chat_list, (SELECT chat_id, max(msg_timestamp) FROM message GROUP BY chat_id) msg WHERE chat_list.member = '%s' AND chat.chat_id = msg.chat_id AND chat_list.chat_id = msg.chat_id ORDER BY max ASC;", user);
		 int rows = esql.executeQueryAndPrintResult(query);

		 if(rows == 0)
			 System.out.println("No chats");

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }  
   }//end 

   public static void NewChat(Messenger esql, String user){
	   try{
		String query = String.format("INSERT INTO chat(chat_type, init_sender) VALUES('private', '%s')", user);
		esql.executeUpdate(query);
	   int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
		query = String.format("INSERT INTO chat_list(chat_id, member) VALUES(%d, '%s')", chat_id, user);
		esql.executeUpdate(query);
	   boolean cont = true;
	   int i = 0;
	  while(cont) {
		System.out.println();
	    System.out.print("\tAdd member to chat: ");
		String member = sanitize_input(in.readLine());

		//does member exist
		query = String.format("Select * from usr where login = '%s'", member);
		int rows = esql.executeQuery(query);
		query = String.format("Select * from chat_list where chat_id = %d AND member = '%s'", chat_id, member);
		int alreadycontains = esql.executeQuery(query);

		if(rows > 0 && member.compareTo(user) != 0 && alreadycontains <=0){
			query = String.format("INSERT INTO chat_list(chat_id, member) VALUES(%d, '%s')", chat_id, member);
			esql.executeUpdate(query);
			System.out.println("\tMember added to chat");
			i++;			
		}
		else{
			if(member.compareTo(user) == 0)
				System.err.println("\tCannot add yourself to chat.");
			else if(alreadycontains > 0)
				System.err.println("\tMember already in chat");
			else
				System.err.println("\tMember does not exist.");
		}


		while(true){
			System.out.print("\tWould you like to add another member? (yes(y) or no(n)) ");
			String input = sanitize_input(in.readLine());
			if(input.compareToIgnoreCase("no")== 0 || input.compareToIgnoreCase("n") == 0){
				cont = false;
				break;
			}
			else if(input.compareToIgnoreCase("yes")== 0 || input.compareToIgnoreCase("y") == 0){
				cont = true;
				break;
			}
			else
				System.err.println("\tUnrecognized command!");
		}

	  }

	  String chat_type = null;
	  if(i > 1){
		  chat_type = "group";
	   query = String.format("UPDATE chat SET chat_type = '%s' WHERE chat_id = %d", chat_type, chat_id);
	   esql.executeUpdate(query);
	  }

	   String message = "Welcome to the chat!";
	   query = String.format("INSERT INTO message(msg_text, sender_login, chat_id) VALUES('%s', '%s', %d)", message, user, chat_id);

	   esql.executeUpdate(query);

	   System.out.println("\tChat created Successfully");

	   } catch(Exception e){
		   System.err.println(e.getMessage());
	   }

	   
   }//end 

   public static void ViewChat(Messenger esql, String user){
	   try{
		 System.out.print("\tEnter chat id to view: ");
         int chat_id = Integer.parseInt(sanitize_input(in.readLine()));

		 String query = String.format("SELECT * FROM chat_list WHERE chat_id = %d AND member='%s'", chat_id, user);
		 int rows = esql.executeQuery(query);
		 if(rows <= 0){
			System.err.println(String.format("Chat %d cannot be viewed.", chat_id));
			return;
		 }

         query = String.format("SELECT * FROM message WHERE chat_id = %d ORDER BY msg_timestamp", chat_id);
		 List<List<String> > chat = esql.executeQueryAndReturnResult(query);

		 boolean cont = true;
		 boolean notendofmessages = true;
		 int count = chat.size();
		 int end = chat.size();
		 while(cont){
			end = count;
			count -= 10;
			if(count < 0){
				count = 0;
				cont = false;
				notendofmessages = false;
			}
		    display10messages(chat, count, end);
			while(notendofmessages){
				System.out.print("\tSrcoll up (up(u)) or quit(q) ");
				String input = sanitize_input(in.readLine());
				if(input.compareToIgnoreCase("quit")== 0 || input.compareToIgnoreCase("q") == 0){
					cont = false;
					break;
				}
				else if(input.compareToIgnoreCase("up")== 0 || input.compareToIgnoreCase("u") == 0){
					break;
				}
				else
					System.err.println("\tUnrecognized command!");
			}	 

			if(notendofmessages == false){
				System.out.println(String.format("End of Messages in Chat %d", chat_id));
			}

		 }

		 

		 if(chat.isEmpty())
			 System.out.println("Chat doesn't exist");

      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 

   }//end 

   public static void display10messages(List<List<String>> chat, int begin, int end){

	   for(int i = begin; i < chat.size() && i< end; i++){
		   String tmp;
		   tmp = String.format("Sender: %s", chat.get(i).get(3));
		   System.out.println(tmp);
		   tmp = String.format("Time: %s", chat.get(i).get(2));
		   System.out.println(tmp);
		   tmp = String.format("Message: %s", chat.get(i).get(1));
		   System.out.println(tmp);
		   System.out.println();
	   }

   }

   public static void EditChat(Messenger esql, String user){
	   //first check if initial sender of chat
	try{
		 System.out.print("\tEnter chat id to edit: ");
         int chat_id = Integer.parseInt(sanitize_input(in.readLine()));

		 String query = String.format("SELECT * FROM chat_list WHERE chat_id = %d AND member='%s'", chat_id, user);
		 int rows = esql.executeQuery(query);
		 if(rows <= 0){
			System.err.println(String.format("Chat %d cannot be viewed.", chat_id));
			return;
		 }

         query = String.format("SELECT init_sender FROM chat WHERE chat_id = %d", chat_id);
		 String init_sender = esql.executeQueryAndReturnResult(query).get(0).get(0);
		 init_sender = init_sender.trim();

		 boolean initial_sender = false;
		 if(init_sender.equals(user) ){
			 initial_sender = true;
		 }

	  boolean chatsmenu = true;
	  while(chatsmenu) {
		System.out.println();
		System.out.println("CHAT EDIT MENU");
		System.out.println("---------");
		System.out.println("1. Send Message.");
		//if initial sender then these options become available
		if(initial_sender){
			System.out.println("2. Add Member to Chat");
			System.out.println("3. Delete Member from Chat");
			System.out.println("4. Delete Chat");
		}
		System.out.println(".........................");
		System.out.println("9. Back");
		switch (readChoice()){
		   case 1: SendMessage(esql, user, chat_id); break;
		   case 2: if(initial_sender) AddMemToChat(esql, user, chat_id); 
		   			else System.out.println("Unrecognized choice!");
					break;
		   case 3: if(initial_sender) DeleteMemFromChat(esql, user, chat_id);
		   			else System.out.println("Unrecognized choice!");
					break;
		   case 4: if(initial_sender) chatsmenu = DeleteChat(esql, user, chat_id);
		   			else System.out.println("Unrecognized choice!");
					break;
		   case 9: chatsmenu = false; break;
		   default : System.out.println("Unrecognized choice!"); break;
		}
	  }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 

   }

//returns false when deleted chat
   public static boolean DeleteChat(Messenger esql, String user, int chat_id){
	   try{
		while(true){
		   System.out.println(String.format("Are you sure you want to delete chat %d? yes(y) or no(n)", chat_id));
			String input = sanitize_input(in.readLine());
			if(input.compareToIgnoreCase("no")== 0 || input.compareToIgnoreCase("n") == 0){
				break;
			}
			else if(input.compareToIgnoreCase("yes")== 0 || input.compareToIgnoreCase("y") == 0){
				//delete chat
				String query = String.format("DELETE FROM chat_list WHERE chat_id=%d", chat_id);
				esql.executeUpdate(query);
				query = String.format("DELETE FROM message WHERE chat_id=%d", chat_id);
				esql.executeUpdate(query);
				query = String.format("DELETE FROM chat WHERE chat_id=%d", chat_id);
				esql.executeUpdate(query);

				System.out.println(String.format("Chat %d deleted successfully!", chat_id));
				return false;

			}
			else
				System.err.println("\tUnrecognized command!");
		}	 

		return true;

	 }catch(Exception e){
		 System.err.println(e.getMessage());
		 return true;
	 }

   }

   public static void SendMessage(Messenger esql, String user, int chat_id){
	   try{
	   System.out.println("Message: ");
	   String message = sanitize_input(in.readLine());
		while(true){
			System.out.print("\tSend (s) or Cancel(c)? ");
			String input = sanitize_input(in.readLine());
			if(input.compareToIgnoreCase("cancel")== 0 || input.compareToIgnoreCase("c") == 0){
				System.out.println("Message not sent.");
				break;
			}
			else if(input.compareToIgnoreCase("send")== 0 || input.compareToIgnoreCase("s") == 0){
				//send message
				String query = String.format("INSERT INTO message(msg_text, sender_login, chat_id) VALUES('%s', '%s', %d)", message, user, chat_id);
				esql.executeUpdate(query);
				System.out.println("Message sent.");
				break;
			}
			else
				System.err.println("\tUnrecognized command!");
		}

	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }
   }

   public static void AddMemToChat(Messenger esql, String user, int chat_id){
	   try{
		   System.out.println("Current members of the chat:");
		   String query = String.format("SElECT member FROM chat_list WHERE chat_id=%d", chat_id);
		   esql.executeQueryAndPrintResult(query);

		   System.out.println("Enter member to add: ");
		   String member = sanitize_input(in.readLine());
		   query = String.format("SELECT * FROM usr WHERE login='%s'", member);
		   int rows = esql.executeQuery(query);
		   query = String.format("SELECT * FROM chat_list WHERE chat_id=%d AND member='%s'", chat_id, member);
		   int inlist = esql.executeQuery(query);
		   if(rows > 0 && inlist <=0){
			   //add member
			   query = String.format("INSERT INTO chat_list(chat_id, member) VALUES(%d, '%s')", chat_id, member);
			   esql.executeUpdate(query);
			   System.out.println(String.format("%s added successfully!", member));

         query = String.format("UPDATE chat SET chat_type = 'group' WHERE chat_id = %d", chat_id);
         esql.executeUpdate(query);

		   }
		   else if(inlist>0){
			   System.err.println("Member already in chat.");
		   }
		   else{
			   System.err.println("Member does not exist.");
		   }

	   }catch(Exception e){
	   		System.err.println(e.getMessage());
	   }
   }//end 

   public static void DeleteMemFromChat(Messenger esql, String user, int chat_id){
	   try{
		   System.out.println("Current members of the chat:");
		  String query = String.format("SElECT member FROM chat_list WHERE chat_id=%d", chat_id);
		  esql.executeQueryAndPrintResult(query);


		   System.out.println("Enter member to delete: ");
		   String member = sanitize_input(in.readLine());
		   query = String.format("SELECT * FROM usr WHERE login='%s'", member);
		   int rows = esql.executeQuery(query);
		   query = String.format("SELECT * FROM chat_list WHERE chat_id=%d AND member='%s'", chat_id, member);
		   int inlist = esql.executeQuery(query);
		   if(rows > 0 && inlist > 0 && member.compareTo(user) != 0){
			   //delete member
			   query = String.format("DELETE FROM chat_list WHERE chat_id=%d AND member='%s'", chat_id, member);
			   esql.executeUpdate(query);
			   System.out.println(String.format("%s deleted successfully!", member));
        
         query = String.format("SELECT * FROM chat_list WHERE chat_id=%d", chat_id);
         rows = esql.executeQuery(query);
		     esql.executeQueryAndPrintResult(query);
         System.out.println(rows);
         if (rows == 2)
         {
           query = String.format("UPDATE chat SET chat_type = 'private' WHERE chat_id = %d", chat_id);
           esql.executeUpdate(query);
         }
         else if (rows == 1)
         {
           query = String.format("DELETE FROM chat_list WHERE chat_id=%d", chat_id);
				   esql.executeUpdate(query);
				   query = String.format("DELETE FROM message WHERE chat_id=%d", chat_id);
				   esql.executeUpdate(query);
           query = String.format("DELETE FROM chat WHERE chat_id=%d", chat_id);
           esql.executeUpdate(query);
         }

		   }
		   else if(inlist<=0){
			   System.err.println("Member not in chat.");
		   }
		   else if(member.compareTo(user) == 0){
			   System.err.println("Cannot delete yourself from the chat.");
		   }
		   else{
			   System.err.println("Member does not exist.");
		   }

	   }catch(Exception e){
	   		System.err.println(e.getMessage());
	   }   
	}//end 

   //--------------------------------------------------------
   //                DELETE ACCOUNT 
   //--------------------------------------------------------

   public static String DeleteAccount(Messenger esql, String user){

      try{
			while(true){
			   System.out.println("Are you sure you want to delete your account? yes(y) or no(n)");
				String input = sanitize_input(in.readLine());
				if(input.compareToIgnoreCase("no")== 0 || input.compareToIgnoreCase("n") == 0){
					break;
				}
				else if(input.compareToIgnoreCase("yes")== 0 || input.compareToIgnoreCase("y") == 0){
					System.out.print("Please enter your password to verify deleting your account: ");
					String password = sanitize_input(in.readLine());
					 String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", user, password);
						 int userNum = esql.executeQuery(query);
					 if (userNum > 0){
						//delete account
						query = String.format("DELETE FROM usr WHERE login='%s' AND password='%s'", user, password);
						esql.executeUpdate(query);
						System.out.println("Your account has been successfully deleted.");
					  return null;
					}
					else{
						System.err.println("Incorrect password.");
					}
					break;
				}
				else
					System.err.println("\tUnrecognized command!");
			}	 
      }catch(Exception e){
        try{
          System.out.println("Your account is still linked to objects. Are you sure you want to continue? yes(y) or no(n)");

				  String input = sanitize_input(in.readLine());
				  if(input.compareToIgnoreCase("no")== 0 || input.compareToIgnoreCase("n") == 0){
					  return user;
				  }
				  else if(input.compareToIgnoreCase("yes")== 0 || input.compareToIgnoreCase("y") == 0){

            String query = String.format("UPDATE usr SET password='!JbB_3a#A)BG?1' WHERE login='%s'", user);
						esql.executeUpdate(query);
						System.out.println("Your account has been successfully deleted.");
            return null;
          }
        }
        catch (Exception e2)
        {
          System.err.println (e2.getMessage ());
        }

		  return user;
      }
	  return user;
   }//end DeleteAccount 

}
