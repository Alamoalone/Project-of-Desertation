import java.io.IOException;
public class AdminModule extends Module
{
	AdminModule(User currentUser)
	{
		this.currentUser= currentUser;
	}
	@Override
	public void startModule() throws IOException
	{
		int choice= -1;
		do
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			choice= Application.inputInt(
				"Admin Module\n"+
				"\t1.Manage Users\n"+
				"\t2.Manage Employees\n"+
				"\t3.Manage Projects\n"+
				"\t4.Manage Employee Type\n"+
				"\t5.Modify Task Phases\n"+
				"\t0.Logout\n"+
				"choose>> "
			);
			switch(choice)
			{
			case 0://exit module
				break;
			case 1:
				manageUsers();
				break;
			case 2:
				manageEmployees();
				break;
			case 3:
				manageProjects();
				break;
			case 4:
				manageEmpType();
				break;
			case 5:
				manageTaskPhases();
				break;
			default:
				System.err.println("\033[31mInvalid Operation!\033[0m");
			}
		} while(choice!=0);
		System.out.print("\033[H\033[2J"); System.out.flush();
	}
	public void manageUsers() throws IOException
	{
		int choice= -1;
	menu_manageUsers:
		do
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			choice= Application.inputInt(
				"Managing Users..\n"+
				"1.Add 2.Update 3.Delete 0.Back\n"+
				"choose>> "
			);
			switch(choice)
			{
			case 0://Back
				break;
			case 1://Add Users
				{
					String uname, pword, utype_str;
					User.utype utype= null;
					int count_users= Application.userDataHandler.getLength();

					while(true)//employee/admin
					{
						utype_str= Application.inputString("User Type [(A)dmin/(E)mployee]: ");
						if(utype_str.equals("exit"))
							continue menu_manageUsers;
						if(utype_str.equals("Admin")||utype_str.equals("admin")||utype_str.equals("A")||utype_str.equals("a"))
							utype= User.utype.admin;
						else if(utype_str.equals("Employee")||utype_str.equals("employee")||utype_str.equals("E")||utype_str.equals("e"))
							utype= User.utype.employee;
						else
						{
							System.err.println("\033[31mInvalid Type! try again or type exit to go back\033[0m\n");
							continue;
						}
						break;
					}
					while(true)//username
					{
						boolean duplicate= false;

						uname= Application.inputString("Username: ");
						if(uname.equals("exit"))
							continue menu_manageUsers;
						for(int k=0;k<count_users;++k)
						{
							User user= Application.userDataHandler.get(k);
							if(user.getUsername().equals(uname))
							{
								duplicate= true;
								break;
							}
						}
						if(duplicate)
						{
							System.err.println("\033[31mUser by that name already exists! try again or type exit to go back\033[0m");
							continue;
						}
						break;
					}
					while(true)//password
					{
						pword= Application.inputString("Password: ");
						if(pword.equals("exit"))
							continue menu_manageUsers;
						if(pword.length()<8)
						{
							System.err.println("\033[31mPassword must be at least 8 character long! try again or type exit to go back\033[0m");
							continue;
						}
						break;
					}
					while(true)//confirm password
					{
						String cpword= Application.inputString("Confirm Password: ");
						if(cpword.equals("exit"))
							continue menu_manageUsers;
						if(!pword.equals(cpword))
						{
							System.err.println("\033[31mPassword Mismatch! try again or type exit to go back\033[0m");
							continue;
						}
						break;
					}
					User user= new User(uname, pword, utype);
					Application.userDataHandler.add(user);
					System.out.print(
						"\033[32mSuccessfully added \""+uname+"\"!\033[0m\n"+
						"Press enter to continue...\n"
					);
					Application.input.nextLine();
				}
				break;
			case 2://Update Users
				{
					User user= null;
					int user_idx= -1;
					int count_users= Application.userDataHandler.getLength();

					if(count_users==0)
					{
						System.out.print(
							"\033[33mNo Registered Users Found!\033[0m\n"+
							"Press Enter to continue...\n"
						);
						Application.input.nextLine();
						break;
					}
					do
					{
						System.out.print("\033[H\033[2J"); System.out.flush();
						System.out.print(
							"Registered Users:\n"+
							"IDX\tUSERNAME\tTYPE\n"
						);
						for(int k=0;k<count_users;++k)
						{
							user= Application.userDataHandler.get(k);
							System.out.printf("%d.\t%s\t%s\n", k+1, user.getUsername(), user.getUserType());
						}
						System.out.println("0. Back");
						user_idx= Application.inputInt("Update>> ")-1;
						if(user_idx==-1)
						 	break;
						if(user_idx<0||user_idx>=count_users)
						{
							System.err.println("\033[31mPlease select a valid number from the users list!\033[0m");
							continue;
						}
						choice= -1;
						while(user_idx!=-1 && choice!=0)
						{
							System.out.print("\033[H\033[2J"); System.out.flush();
							choice= Application.inputInt(
								"1. Username: "+user.getUsername()+"\n"+
								"2. Type:     "+user.getUserType()+"\n"+
								"3. Password\n"+
								"0. Cancel\n"+
								"Modify>> "
							);
							switch(choice)
							{
							case 0://Cancel
								break;
							case 1://modify username
								while(true)
								{
									String newname;
									Boolean duplicate= false;

									newname= Application.inputString("New Username: ");
									if(newname.equals("exit"))
										break;
									for(int k=0;k<count_users;++k)
									{
										User compare= Application.userDataHandler.get(k);
										if(compare.getUsername().equals(newname))
										{
											duplicate= true;
											break;
										}
									}
									if(duplicate)
									{
										System.err.println("\033[31mUser Already Exists! try again or enter exit to go back\033[0m");
										continue;
									}
									user.setUsername(newname);
									Application.userDataHandler.update(user_idx, user);
									break;
								}
								break;
							case 2://modify type
								while(true)
								{
									String newtype;

									newtype= Application.inputString("New Type [(A)dmin/(E)mployee]: ");
									if(newtype.equals("exit"))
										break;
									if(newtype.equals("Admin")||newtype.equals("admin")||newtype.equals("A")||newtype.equals("a"))
										user.setUserType(User.utype.admin);
									else if(newtype.equals("Employee")||newtype.equals("employee")||newtype.equals("E")||newtype.equals("e"))
										user.setUserType(User.utype.employee);
									else
									{
										System.out.println("\033[31mInvalid User Type! try again or type \"exit\" to go back\033[0m");
										continue;
									}
									Application.userDataHandler.update(user_idx, user);
									break;
								}
								break;
							case 3://modify password
								while(true)
								{
									String oldpword, newpword;

									oldpword= Application.inputString("Old Password: ");
									if(oldpword.equals("exit"))
										break;
									if(!oldpword.equals(user.getPassword()))
									{
										System.out.println("\033[31mIncorrect Password! try again or type \"exit\" to go back\033[0m");
										continue;
									}
									newpword= Application.inputString("New Password: ");
									if(newpword.equals("exit"))
										break;
									user.setPassword(newpword);
									Application.userDataHandler.update(user_idx, user);
									break;
								}
								break;
							default:
								System.err.println("\033[31mInvalid Operation!\033[0m");
								break;
							}
						}
					} while(user_idx!=-1);
				}
				break;
			case 3://Delete Users
				while(true)
				{
					User user= null;
					int user_idx= -1;
					String uname;
					int
						count_users= Application.userDataHandler.getLength(),
						count_employees= Application.employeeDataHandler.getLength(),//cascade delete
						count_tasks= Application.taskDataHandler.getLength(),//nullify
						count_projects= Application.projectDataHandler.getLength();//nullify

					if(count_users==0)
					{
						System.out.print(
							"\033[33mNo Registered Users Found!\033[0m\n"+
							"Press enter to continue..."
						);
						Application.input.nextLine();
						break;
					}
					System.out.print("\033[H\033[2J"); System.out.flush();
					System.out.print(
						"Registered Users:\n"+
						"IDX\tUSERNAME\tTYPE\n"
					);
					for(int k=0;k<count_users;++k)
					{
						user= Application.userDataHandler.get(k);
						System.out.printf("%d.\t%s\t%s\n", k+1, user.getUsername(), user.getUserType());
					}
					System.out.println("0. Cancel");
					user_idx= Application.inputInt("Delete>> ")-1;
					if(user_idx==-1)
						break;
					if(user_idx<0||user_idx>=count_users)
					{
						System.err.println("\033[31mPlease select a valid number from the users list!\033[0m");
						continue;
					}
					uname= user.getUsername();
					String confirm= Application.inputString("Are you sure you want to \033[31mDELETE\033[0m \""+uname+"\"? [Y/N]: ");
					if(!confirm.equals("Y") && !confirm.equals("y"))//don't delete
						continue;
					Application.userDataHandler.delete(user_idx);//delete user

					for(int k=0;k<count_employees;++k)//delete from employees
					{
						Employee employee= Application.employeeDataHandler.get(k);
						if(employee.getUsername().equals(uname))
						{
							Application.employeeDataHandler.delete(k);
							break;
						}
					}
					for(int k=0;k<count_tasks;++k)//nullify assigned tasks
					{
						Task task= Application.taskDataHandler.get(k);
						if(task.getAssignedEmployee().getUsername().equals(uname))
						{
							task.setAssignedEmployee(null);
							Application.taskDataHandler.update(k, task);
						}
					}
					for(int k=0;k<count_projects;++k)//nullify any assigned projects 
					{
						Project project= Application.projectDataHandler.get(k);
						if(project.getLeader().getUsername().equals(uname))
						{
							project.setLeader(null);
							Application.projectDataHandler.update(k, project);
						}
					}
				}
				break;
			default:
				System.err.println("\033[31mInvalid Operation!\033[0m");
			}
		} while(choice!=0);
	}
	public void manageEmployees() throws IOException
	{
		int choice= 0;
		do
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			choice= Application.inputInt(
				"Managing Employees..\n"+
				"1.Add  2.Update  3.Delete  0.Back\n"+
				"choose>> "
			);
			switch(choice)
			{
			case 1://Add Employees
				{
					Employee employee= null;
					User user= null;
					String uname= null;
					int
						empType_idx= -1,
						count_users= Application.userDataHandler.getLength(),
						count_emptypes= Application.empTypeDataHandler.getLength();
					boolean unregistered= false;
					
					if(count_users==0)
					{
						System.out.print(
							"\033[33mNo Registered Users Found!\033[0m\n"+
							"Press enter to continue...\n"
						);
						Application.input.nextLine();
						break;//back to menu
					}
					do
					{
						int count_employees= Application.employeeDataHandler.getLength();
						for(int i=0;i<count_users;i++)
						{
							boolean found= false;
							User user_registered= Application.userDataHandler.get(i);

							if(user_registered.getUserType()!=User.utype.employee)
								continue;
							for(int j=0;j<count_employees;j++)
							{
								Employee employee_registered= Application.employeeDataHandler.get(j);
								if(employee_registered.getUsername().equals(user_registered.getUsername()))
								{
									found= true;
									break;
								}
							}
							if(!found)
							{
								unregistered= true;
								break;
							}
						}
						if(!unregistered)
						{
							System.out.print(
								"\033[33mAll employee user accounts have already been registered & approved!\033[0m\n"+
								"Enter any key to continue...\n"
							);
							Application.input.nextLine();
							break;//back to manageEmployees;
						}
						System.out.print("\033[H\033[2J"); System.out.flush();
						System.out.print(
							"Registered Accounts:\n"+
							"USERNAME\tTYPE\n"
						);
						for(int k=0;k<count_users;++k)
						{
							user= Application.userDataHandler.get(k);
							if(user.getUserType()!=User.utype.employee)
								continue;

							unregistered= false;
							for(int i=0;i<count_employees;++i)
							{
								if(Application.employeeDataHandler.get(i).getUsername().equals(user.getUsername()))
								{
									unregistered= true;
									break;
								}
							}
							if(!unregistered)
								System.out.printf("%s\t%s\n", user.getUsername(), user.getUserType());
						}
						System.out.println("\"exit\" to go back");
						uname= Application.inputString("Choose>> ");
						if(uname.equals("exit"))
							break;
						Boolean found= false;
						for(int k=0;k<count_users;++k)
						{
							user= Application.userDataHandler.get(k);
							if(user.getUsername().equals(uname))
							{
								found= true;
								break;
							}
						}
						if(!found)
						{
							System.err.println("\033[31mPlease select a valid user account from the list!\033[0m");
							continue;
						}
						if(count_emptypes==0)
						{
							employee= new Employee(user.getUsername(), user.getPassword(), User.utype.employee, null);
							Application.employeeDataHandler.add(employee);
						}
						else
						{
							do
							{
								EmpType empType= null;

								System.out.print("\033[H\033[2J"); System.out.flush();
								System.out.print(
									"Defined Employee Types:\n"+
									"IDX\tTYPE\tMANAGER?\n"
								);
								for(int k=0;k<count_emptypes;++k)
								{
									empType= Application.empTypeDataHandler.get(k);
									System.out.printf("%d.\t%s\t%s\n", k+1, empType.getName(), empType.isManager()?"Yes":"No");
								}
								System.out.print(
									"0. Cancel\n"+
									"-1. TBD\n"
								);
								empType_idx= Application.inputInt("Choose>> ")-1;
								if(empType_idx==-1)
									break;
								if(empType_idx==-2)
								{
									employee= new Employee(user.getUsername(), user.getPassword(), User.utype.employee, null);
									Application.employeeDataHandler.add(employee);
									break;
								}
								if(empType_idx<0||empType_idx>=count_emptypes)
								{
									System.err.println("\033[31mPlease select a valid number from the employee type list!\033[0m");
									continue;
								}
								empType= Application.empTypeDataHandler.get(empType_idx);
								employee= new Employee(user.getUsername(), user.getPassword(), User.utype.employee, empType);
								Application.employeeDataHandler.add(employee);
								break;
							} while(empType_idx!=-1);
						}
					} while(!uname.equals("exit"));
				}
				break;
			case 2://Update Employees
				{
					Employee employee= null;
					int
						employee_idx= -1,
						empType_idx= -1,
						count_employees= Application.employeeDataHandler.getLength();
						
					if(count_employees==0)
					{
						System.out.print(
							"\033[33mNo Employees Registered!\033[0m\n"+
							"Press Enter to continue...\n"
						);
						Application.input.nextLine();
						break;
					}
					do
					{
						System.out.print("\033[H\033[2J"); System.out.flush();
						System.out.print(
							"Registered Employees:\n"+
							"IDX\tNAME\tPOSITION\n"
						);
						for(int k=0;k<count_employees;++k)
						{
							employee= Application.employeeDataHandler.get(k);
							System.out.printf(
								"%d.\t%s\t%s\n",
								k+1,
								employee.getUsername(),
								employee.getEmpType()==null?"TBD":employee.getEmpType().getName()
							);
						}
						System.out.println("0. Back");
						employee_idx= Application.inputInt("Update>> ")-1;
						if(employee_idx==-1)
						 	break;
						if(employee_idx<0||employee_idx>=count_employees)
						{
							System.err.println("\033[31mPlease select a valid number from the employees list!\033[0m");
							continue;
						}
						do
						{
							EmpType empType= null;
							int count_emptypes= Application.empTypeDataHandler.getLength();

							System.out.print("\033[H\033[2J"); System.out.flush();
							System.out.print(
								"Defined Employee Types:\n"+
								"IDX\tTYPE\tMANAGER?\n"
							);
							for(int k=0;k<count_emptypes;++k)
							{
								empType= Application.empTypeDataHandler.get(k);
								System.out.printf("%d.\t%s\t%s\n", k+1, empType.getName(), empType.isManager()?"Yes":"No");
							}
							System.out.print(
								"0. Cancel\n"+
								"-1. TBD\n"
							);
							empType_idx= Application.inputInt("Choose>> ")-1;
							if(empType_idx==-1)
								break;
							if(empType_idx==-2)
							{
								employee.setEmpType(null);
								Application.employeeDataHandler.update(employee_idx, employee);
								break;
							}
							if(empType_idx<0||empType_idx>=count_emptypes)
							{
								System.err.println("\033[31mPlease select a valid number from the employee type list!\033[0m");
								continue;
							}
							empType= Application.empTypeDataHandler.get(empType_idx);
							employee.setEmpType(empType);
							Application.employeeDataHandler.update(employee_idx, employee);
							break;
						} while(empType_idx!=-1);
					} while(employee_idx!=-1);
				}
				break;
			case 3://Delete Employees
				while(true)
				{
					Employee employee= null;
					int
						employee_idx= -1,
						count_employees= Application.employeeDataHandler.getLength(),
						count_users= Application.userDataHandler.getLength(),//for cascade deletion
						count_tasks= Application.taskDataHandler.getLength(),//for nullifying
						count_projects= Application.projectDataHandler.getLength();//for nullifying

					if(count_employees==0)
					{
						System.out.print(
							"\033[33mNo Employees Yet!\033[0m\n"+
							"Press enter to continue...\n"
						);
						Application.input.nextLine();
						break;//back to menu
					}
					System.out.print("\033[H\033[2J"); System.out.flush();
					System.out.print(
						"Registered Employees:\n"+
						"IDX\tUSERNAME\tTYPE\n"
					);
					for(int k=0;k<count_employees;++k)
					{
						employee= Application.employeeDataHandler.get(k);
						System.out.printf("%d.\t%s\t%s\n", k+1, employee.getUsername(), employee.getEmpType()==null?"TBD":employee.getEmpType().getName());
					}
					System.out.println("0. Cancel");
					employee_idx= Application.inputInt("Delete>> ")-1;
					if(employee_idx==-1)
						break;
					if(employee_idx<0||employee_idx>=count_employees)
					{
						System.err.println("\033[31mPlease select a valid number from the users list!\033[0m");
						continue;
					}
					employee= Application.employeeDataHandler.get(employee_idx);
					String confirm= Application.inputString("Are you sure you want to \033[31mDELETE\033[0m \""+employee.getUsername()+"\"? [y/N]: ");
					if(!confirm.equals("Y") && !confirm.equals("y"))//don't delete
						continue;
					Application.employeeDataHandler.delete(employee_idx);//delete employee
					for(int k=0;k<count_users;++k)
					{
						User user= Application.userDataHandler.get(k);
						if(user.getUsername().equals(employee.getUsername()))//delete user account
						{
							Application.userDataHandler.delete(k);
							break;
						}
					}
					for(int k=0;k<count_tasks;++k)//nullify assigned tasks
					{
						Task task= Application.taskDataHandler.get(k);
						if(task.getAssignedEmployee().getUsername().equals(employee.getUsername()))
						{
							task.setAssignedEmployee(null);
							Application.taskDataHandler.update(k, task);
						}
					}
					for(int k=0;k<count_projects;++k)//nullify any assigned projects 
					{
						Project project= Application.projectDataHandler.get(k);
						if(project.getLeader().getUsername().equals(employee.getUsername()))
						{
							project.setLeader(null);
							Application.projectDataHandler.update(k, project);
						}
					}
				}
				break;
			case 4:
				break;
			default:
				System.out.println("\033[31mInvalid Operation!\033[0m");
			}
		} while(choice!=0);
	}
	public void manageProjects() throws IOException
	{
		int choice= 0;
		boolean exit= false;
	menu_manageProjects:
		while(!exit)
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			System.out.print(
				"Managing Projects..\n"+
				"1.Add 2.Update 3.Delete 0.Back\n"+
				"input>> ");
			try
			{
				choice= Integer.parseInt(Application.input.nextLine());
			}
			catch(NumberFormatException e)
			{
				System.out.println("\033[31mIllegal Character!\033[0m");
				continue;
			}
			switch(choice)
			{
			case 0://Back
				exit= true;
				break;
			case 1://Add Projects
				{
					String project_name, project_description;
					int count_employees= Application.employeeDataHandler.getLength();
					Employee leader= null;

					while(true)//project name
					{
						boolean duplicate= false;
						int count_projects= Application.projectDataHandler.getLength();

						System.out.print("Project Name: ");
						project_name= Application.input.nextLine().trim();
						if(project_name.isBlank())
						{
							System.out.print(
								"\033[31mThe project must have a name!\033[0m\n"+
								"\033[33mTry Again? [Y/N]: \033[0m"
							);
							String retry= Application.input.nextLine();
							if(retry.equals("Y")||retry.equals("y"))
								continue;
							continue menu_manageProjects;
						}
						for(int k=0;k<count_projects;++k)
						{
							Project project= Application.projectDataHandler.get(k);
							if(project.getName().equals(project_name))
							{
								duplicate= true;
								break;
							}
						}
						if(duplicate)
						{
							System.out.print(
								"\033[31mProject with the same name already in progress!\033[0m\n"+
								"\033[33mTry again? [Y/N]: \033[0m"
							);
							String retry= Application.input.nextLine();
							if(retry.equals("Y")||retry.equals("y"))
								continue;
							continue menu_manageProjects;
						}
						break;
					}
					while(true)//project description
					{
						System.out.print("Project Description: ");
						project_description= Application.input.nextLine();
						if(project_description.isBlank())
						{
							System.out.print(
								"\033[31mThe project must have a description!\033[0m\n"+
								"\033[33mTry again? [Y/N]: \033[0m"
							);
							String retry= Application.input.nextLine();
							if(retry.equals("Y")||retry.equals("y"))
								continue;
							continue menu_manageProjects;
						}
						break;
					}
					if(count_employees!=0)//project leader
					{
						int leader_idx;
						System.out.print(
							"Available employees to assign a leader:\n"+
							"IDX\tNAME\tPOSITION\tMANAGER?\n"
						);
						for(int k=0;k<count_employees;++k)
						{
							Employee employee= Application.employeeDataHandler.get(k);
							System.out.printf("%d.\t%s\t%s\n", k+1, employee.getUsername(), employee.getEmpType());
						}
						System.out.println("0. Don\'t assign anyone yet");
						while(true)
						{
							System.out.print("Input>> ");
							try
							{
								leader_idx= Integer.parseInt(Application.input.nextLine())-1;
							}
							catch(NumberFormatException e)
							{
								System.out.println("\033[31mPlease select a valid number from the employees list!\033[0m");
								continue;
							}
							if(leader_idx==-1)
								break;
							if(leader_idx<0||leader_idx>=count_employees)
							{
								System.out.println("\033[31mPlease select a valid number from the employees list!\033[0m");
								continue;
							}
							leader= Application.employeeDataHandler.get(leader_idx);
							break;
						}
					}
					Project project= new Project(project_name, project_description, leader);
					Application.projectDataHandler.add(project);
					System.out.print(
						"\033[32mSuccessfully added project \""+project_name+"\"!\033[0m\n"+
						"Press any key to continue...\n"
					);
					System.in.read();
				}
				break;
			case 2://Update Projects
			menu_manageProjects_update:
				while(true)
				{
					Project project;
					int
						count_projects= Application.projectDataHandler.getLength(),
						project_idx= -1;

					if(count_projects==0)
					{
						System.out.print(
							"\033[33mNo Projects Found!\033[0m\n"+
							"Press any key to continue..."
						);
						System.in.read();
						break;
					}
					System.out.print("\033[H\033[2J"); System.out.flush();
					System.out.print(
						"Projects in progress:\n"+
						"IDX\tNAME\tLEADER\tDESCRIPTION\n"
					);
					for(int k=0;k<count_projects;++k)
					{
						project= Application.projectDataHandler.get(k);
						System.out.printf("%d.\t%s\t%s\t%s\n", k+1, project.getName(), project.getLeader()==null?"\033[33mTBD\033[0m":project.getLeader().getUsername(), project.getDescription());
					}
					System.out.println("0. Back");
					while(true)
					{
						System.out.print("Update>> ");
						try
						{
							project_idx= Integer.parseInt(Application.input.nextLine())-1;
						}
						catch(NumberFormatException e)
						{
							System.out.println("\033[31mPlease select a valid number from the projects list!\033[0m");
							continue;
						}
						if(project_idx==-1)
							continue menu_manageProjects;
						if(project_idx<0||project_idx>=count_projects)
						{
							System.out.println("\033[31mPlease select a valid number from the projects list!\033[0m");
							continue;
						}
						project= Application.projectDataHandler.get(project_idx);
						while(true)
						{
							System.out.print("\033[H\033[2J"); System.out.flush();
							System.out.printf(
								"\t1. Name:        %s\n"+
								"\t2. Description: %s\n"+
								"\t3. Leader:      %s\n"+
								"\t0. Cancel\n"+
								"Modify>> ",
								project.getName(),
								project.getDescription(),
								project.getLeader()==null?"\033[33mTBD\033[0m":project.getLeader().getUsername()
							);
							try
							{
								choice= Integer.parseInt(Application.input.nextLine());
							}
							catch(NumberFormatException e)
							{
								System.out.println("\033[31mInvalid Operation!\033[0m");
								continue;
							}
							switch(choice)
							{
							case 0://Cancel
								continue menu_manageProjects_update;
							case 1://Modify Project Name
								while(true)
								{
									boolean duplicate= false;
									String newname;

									System.out.print("\tNew Name: ");
									newname= Application.input.nextLine().trim();
									if(newname.isBlank())
									{
										System.out.print(
											"\033[31mThe project must have a name!\033[0m\n"+
											"\033[33mTry Again? [Y/N]: \033[0m"
										);
										String retry= Application.input.nextLine();
										if(retry.equals("Y")||retry.equals("y"))
											continue;
										break;
									}
									for(int k=0;k<count_projects;++k)
									{
										Project compare= Application.projectDataHandler.get(k);
										if(compare.getName().equals(newname))
										{
											duplicate= true;
											break;
										}
									}
									if(duplicate)
									{
										System.out.print(
											"\033[31mProject with the same name already in progress!\033[0m\n"+
											"\033[33mTry again? [Y/N]: \033[0m"
										);
										String retry= Application.input.nextLine();
										if(retry.equals("Y")||retry.equals("y"))
											continue;
										break;
									}
									project.setName(newname);
									Application.projectDataHandler.update(project_idx, project);
									break;
								}
								break;
							case 2://Modify Project Description
								while(true)
								{
									System.out.print("New Description: ");
									String newdescription= Application.input.nextLine().trim();
									if(newdescription.isBlank())
									{
										System.out.print(
											"\033[31mThe project must have a brief description!\033[0m\n"+
											"\033[33mTry Again? [Y/N]: \033[0m"
										);
										String retry= Application.input.nextLine();
										if(retry.equals("Y")||retry.equals("y"))
											continue;
										break;
									}
									project.setDescription(newdescription);
									Application.projectDataHandler.update(project_idx, project);
									break;
								}
								break;
							case 3://Modify Project Leader
								{
									int
										leader_idx= -1,
										count_employees= Application.employeeDataHandler.getLength();
									
									if(count_employees==0)
									{
										System.out.print(
											"\033[33mNo employees available\033[0m\n"+
											"Press any key to continue...\n"
										);
										System.in.read();
										break;
									}
									System.out.print(
										"Available employees to assign a leader:\n"+
										"IDX\tNAME\tPOSITION\tMANAGER?\n"
									);
									for(int k=0;k<count_employees;++k)
									{
										Employee employee= Application.employeeDataHandler.get(k);
										System.out.printf("%d.\t%s\t%s\n", k+1, employee.getUsername(), employee.getEmpType());
									}
									System.out.println("0. Don\'t assign anyone");
									while(true)
									{
										System.out.print("Input>> ");
										try
										{
											leader_idx= Integer.parseInt(Application.input.nextLine())-1;
										}
										catch(NumberFormatException e)
										{
											System.out.println("\033[31mPlease select a valid number from the employees list!\033[0m");
											continue;
										}
										if(leader_idx==-1)
										{
											project.setLeader(null);
											Application.projectDataHandler.update(project_idx, project);
											break;
										}
										if(leader_idx<0||leader_idx>=count_employees)
										{
											System.out.println("\033[31mPlease select a valid number from the employees list!\033[0m");
											continue;
										}
										project.setLeader(Application.employeeDataHandler.get(leader_idx));
										Application.projectDataHandler.update(project_idx, project);
										break;
									}
								}
								break;
							default:
								System.out.println("\033[31mInvalid Operation!\033[0m");
								break;
							}
						}
					}
				}
				break;
			case 3://Delete Projects
				{
					Project project;
					int
						count_projects= Application.projectDataHandler.getLength(),
						count_tasks= Application.taskDataHandler.getLength(),//for cascade delete
						project_idx= -1;

					if(count_projects==0)
					{
						System.out.println("\033[33mNo Projects Found!\033[0m\nEnter any key to continue...");
						Application.input.nextLine();
						continue menu_manageProjects;
					}
					System.out.print("\033[H\033[2J");
					System.out.flush();
					System.out.print(
						"Projects in progress:\n"+
						"IDX\tNAME\tLEADER\tDESCRIPTION\n"
					);
					for(int k=0;k<count_projects;++k)
					{
						project= Application.projectDataHandler.get(k);
						System.out.println((k+1)+".\t"+project.getName()+"\t"+project.getLeader().getUsername()+"\t"+project.getDescription());
					}
					System.out.println("0. Cancel");
					while(true)
					{
						System.out.print("Delete>> ");
						try
						{
							project_idx= Integer.parseInt(Application.input.nextLine())-1;
						}
						catch(NumberFormatException e)
						{
							System.out.println("\033[31mPlease select a valid number from the projects list!\033[0m");
							continue;
						}
						if(project_idx==-1)
							break;
						if(project_idx<0||project_idx>=count_projects)
						{
							System.out.println("\033[31mPlease select a valid number from the projects list!\033[0m");
							continue;
						}
						project= Application.projectDataHandler.get(project_idx);
						System.out.print("Are you sure you want to \033[31mDELETE\033[0m \""+project.getName()+"\"? [Y/N]: ");
						String confirm= Application.input.nextLine();
						if(!confirm.equals("Y") && !confirm.equals("y"))//don't delete
							continue;
						Application.projectDataHandler.delete(project_idx);//delete project
						for(int k=0;k<count_tasks;++k)//delete all tasks associated with the project
						{
							Task task= Application.taskDataHandler.get(k);
							if(task.getProject()==project)
								Application.taskDataHandler.delete(k);
						}
					}
				}
				break;
			default:
				System.out.println("\033[31mInvalid Operation!\033[0m");
			}
		}
	}
	public void manageEmpType() throws IOException
	{
		int choice= 0;
		boolean exit= false;
	menu_manageEmptype:
		while(!exit)
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			System.out.print(
				"Managing Types of Employees..\n"+
				"1.Add  2.Update  3.Delete  0.Back\n"+
				"input>> "
			);
			try
			{
				choice= Integer.parseInt(Application.input.nextLine());
			}
			catch(NumberFormatException e)
			{
				System.out.println("\033[31mInvalid Operation!\033[0m");
				continue;
			}
			switch(choice)
			{
			case 0:
				exit= true;
				break;
			case 1://Add Employee Type
				{
					EmpType empType= null;
					String empString= null;
					
					while(true)
					{
						boolean duplicate= false;
						int count_emptypes= Application.empTypeDataHandler.getLength();
						System.out.print("New Type of Employee: ");
						empString= Application.input.nextLine();
						for(int k=0;k<count_emptypes;++k)
						{
							EmpType compare= Application.empTypeDataHandler.get(k);
							if(compare.getName().equals(empString))
							{
								duplicate= true;
								break;
							}
						}
						if(duplicate)
						{
							System.out.print(
								"\033[31mType with the same name already exists!\033[0m\n"+
								"\033[33mTry again? [Y/N]: \033[0m"
							);
							String retry= Application.input.next();
							if(retry.equals("Y")||retry.equals("y"))
								continue;
							else
								continue menu_manageEmptype;
						}
						break;
					}
					System.out.print("Categorize as manager? [y/N]: ");
					String isManager= Application.input.nextLine();
					if(isManager.equals("Y")||isManager.equals("y"))
						empType= new EmpType(empString, true);
					else
						empType= new EmpType(empString, false);
					Application.empTypeDataHandler.add(empType);
					System.out.print(
						"\033[32mSuccessfully added employee type \""+empString+"\"!\033[0m\n"+
						"Press any key to continue...\n"
					);
					System.in.read();
				}
				break;
			case 2://Update Employee Type
			menu_manageEmptype_update:
				while(true)
				{
					EmpType emptype= null;
					int emptype_idx= -1;
					int count_emptype= Application.empTypeDataHandler.getLength();

					if(count_emptype==0)
					{
						System.out.print(
							"\033[33mNo Employee Types Defined Yet!\033[0m\n"+
							"Press any key to continue...\n"
						);
						System.in.read();
						break;
					}
					System.out.print("\033[H\033[2J"); System.out.flush();
					System.out.print(
						"Defined Employee Types:\n"+
						"IDX\tTYPE\tMANAGER?\n"
					);
					for(int k=0;k<count_emptype;++k)
					{
						emptype= Application.empTypeDataHandler.get(k);
						System.out.printf("%d.\t%s\t%s\n", k+1, emptype.getName(), emptype.isManager()?"Yes":"No");
					}
					System.out.println("0. Cancel");
					while(true)
					{
						System.out.print("Update>> ");
						try
						{
							emptype_idx= Integer.parseInt(Application.input.nextLine())-1;
						}
						catch(NumberFormatException e)
						{
							System.out.println("\033[31mPlease select a valid number from the type list!\033[0m");
							continue;
						}
						if(emptype_idx==-1)
							continue menu_manageEmptype;
						if(emptype_idx<0||emptype_idx>=count_emptype)
						{
							System.out.println("\033[31mPlease select a valid number from the type list!\033[0m");
							continue;
						}
						emptype= Application.empTypeDataHandler.get(emptype_idx);
						while(true)
						{
							System.out.print("\033[H\033[2J"); System.out.flush();
							System.out.printf(
								"1. Type    : %s\n"+
								"2. Manager?: %s\n"+
								"0. Cancel\n"+
								"Modify>> ",
								emptype.getName(),
								emptype.isManager()?"Yes":"No"
							);
							try
							{
								choice= Integer.parseInt(Application.input.nextLine());
							}
							catch(NumberFormatException e)
							{
								System.out.println("\033[31mInvalid Operation!\033[0m");
								continue;
							}
							switch(choice)
							{
							case 0:
								continue menu_manageEmptype_update;
							case 1://Modify Type
								while(true)
								{
									String newtype, oldtype= emptype.getName();
									int count_employees= Application.employeeDataHandler.getLength();
									boolean duplicate= false;

									System.out.print("New Type: ");
									newtype= Application.input.nextLine();
									
									for(int k=0;k<count_emptype;++k)
									{
										EmpType compare= Application.empTypeDataHandler.get(k);
										if(compare.getName().equals(newtype))
										{
											duplicate= true;
											break;
										}
									}
									if(duplicate)
									{
										System.out.print(
											"\033[31mType with the same name already in exists!\033[0m\n"+
											"\033[33mTry again? [Y/N]: \033[0m"
										);
										String retry= Application.input.nextLine();
										if(retry.equals("Y")||retry.equals("y"))
											continue;
										break;
									}
									emptype.setName(newtype);
									Application.empTypeDataHandler.update(emptype_idx, emptype);
									for(int k=0;k<count_employees;++k)//update all employees with this type
									{
										Employee employee= Application.employeeDataHandler.get(k);
										if(employee.getEmpType().getName().equals(oldtype))
										{
											employee.setEmpType(emptype);
											Application.employeeDataHandler.update(k, employee);
											System.out.printf("\033[33m\"%s\" has been promoted to \"%s\"!\033[0m\n", employee.getUsername(), newtype);
										}
									}
									System.out.print(
										"\033[33mSuccessfully updated employee type!\033[0m\n"+
										"Press any key to continue...\n"
									);
									System.in.read();
									break;
								}
								break;
							case 2://Modify Managerial Position
								{
									String isManager= null;
									System.out.print("Categorize as manager? [y/N]: ");
									isManager= Application.input.nextLine();
									if(isManager.equals("Y")||isManager.equals("y"))
										emptype.setManager(true);
									else
										emptype.setManager(false);
									Application.empTypeDataHandler.update(emptype_idx, emptype);
									System.out.print(
										"\033[32mSuccessfully modified managerial position!\033[0m\n"+
										"Press any key to continue...\n"
									);
									System.in.read();
								}
								break;
							default:
								System.out.println("\033[31mInvalid Operation!\033[0m");
								break;
							}
							break;
						}
						break;
					}
				}
			case 3://Delete Employee Type
				{
					EmpType emptype= null;
					int
						emptype_idx= -1,
						count_emptypes= Application.empTypeDataHandler.getLength(),
						count_employees= Application.employeeDataHandler.getLength();//for nullifying

					if(count_emptypes==0)
					{
						System.out.print(
							"\033[33mNo Employee Types Defined Yet!\033[0m\n"+
							"Enter any key to continue...\n"
						);
						Application.input.nextLine();
						break;
					}
					System.out.print("\033[H\033[2J"); System.out.flush();
					System.out.print(
						"Defined Employee Types:\n"+
						"IDX\tTYPE\tMANAGER?\n"
					);
					for(int k=0;k<count_emptypes;++k)
					{
						emptype= Application.empTypeDataHandler.get(k);
						System.out.printf("%d.\t%s\t%s\n", k+1, emptype.getName(), emptype.isManager()?"Yes":"No");
					}
					System.out.println("0. Cancel");
					while(true)
					{
						System.out.print("Delete>> ");
						try
						{
							emptype_idx= Integer.parseInt(Application.input.nextLine())-1;
						}
						catch(NumberFormatException e)
						{
							System.out.println("\033[31mPlease select a valid number from the type list!\033[0m");
							continue;
						}
						if(emptype_idx==-1)
							break;
						if(emptype_idx<0||emptype_idx>=count_emptypes)
						{
							System.out.println("\033[31mPlease select a valid number from the type list!\033[0m");
							continue;
						}
						emptype= Application.empTypeDataHandler.get(emptype_idx);
						System.out.printf("Are you sure you want to \033[31mDELETE\033[0m \"%s\" type? [y/N]: ", emptype.getName());
						String confirm= Application.input.nextLine();
						if(!confirm.equals("Y") && !confirm.equals("y"))//Don't Delete
							break;
						Application.empTypeDataHandler.delete(emptype_idx);//delete type
						for(int k=0;k<count_employees;++k)//nullify all employees with this type
						{
							Employee employee= Application.employeeDataHandler.get(k);
							if(employee.getEmpType().getName().equals(emptype.getName()));
							{
								employee.setEmpType(null);
								Application.employeeDataHandler.update(k, employee);
								System.out.printf("\033[33m\"%s\" type has been nullified!\033[0m\n", employee.getUsername());
							}
						}
						System.out.println("Press any key to continue...");
						System.in.read();
					}
				}
				break;
			default:
				System.out.println("\033[31mInvalid Operation!\033[0m");
			}
		}
	}
	public void manageTaskPhases() throws IOException
	{
		Task task= null;
		int
			task_idx= -1,
			count_tasks= Application.taskDataHandler.getLength();

		if(count_tasks==0)
		{
			System.out.print(
				"\033[33mNo Tasks Yet!\033[0m\n"+
				"Press enter to continue..."
			);
			Application.input.nextLine();
			return;//to menu
		}
		while(true)
		{
			System.out.print("\033[H\033[2J"); System.out.flush();
			System.out.print(
				"Current Tasks:\n"+
				"IDX\tCODE\tTITLE\tPROJECT\tPRIORITY\tPHASE\tDESCRIPTION\tSTARTS\tENDS\tEST\tCREATOR\tASSIGNED\n"
			);
			for(int k=0;k<count_tasks;++k)
			{
				task= Application.taskDataHandler.get(k);
				System.out.printf(
					"%d.\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%f\t%s\t%s\n",
					k+1,
					task.getCode(),
					task.getTitle(),
					task.getProject().getName(),
					task.getPriority().toString(),
					task.getTaskPhase(),
					task.getDescription(),
					task.getStartDate().toString(),
					task.getEndDate().toString(),
					task.getEST(),
					task.getCreator().getUsername(),
					task.getAssignedEmployee().getUsername()
				);
			}
			System.out.println("0. Cancel");
			task_idx= Application.inputInt("Modify>> ")-1;
			if(task_idx==-1)
				break;
			if(task_idx<0||task_idx>=count_tasks)
			{
				System.err.println("\033[31mPlease select a valid number from the tasks list!\033[0m");
				continue;
			}
			task= Application.taskDataHandler.get(task_idx);
			String phase= Application.inputString("New Task Phase: ");
			task.setTaskPhase(phase);
			Application.taskDataHandler.update(task_idx, task);
		}
	}
}