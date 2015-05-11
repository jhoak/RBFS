Hello and welcome to the RBFS page. 
This project implements the NIST model of role-based access control, 
a form of access control in computer security that indirectly assigns 
access permissions to users based on their "roles" in the system 
(which can be likened to their jobs in an organization, or their authority). 
The project uses a Java client and server with a Microsoft SQL Server 2008-R2 
backend, with the Microsoft JDBC driver for SQL Server (ver. 4.1).

The implementation of the NIST model is pretty complete here, although some 
work needs to be done on fixing small bugs and extending the implementation 
to handle more databases, as well as a dynamic set of user-defined roles.

For more information on role-based access control, see the following papers
(The former introduced the idea of RBAC in 1996, and the latter proposed a
NIST standard for it in 2001):

Ferraiolo, D.F., Sandhu, R.S., and Gavrila, S. Proposed NIST standard for 
  role-based access control. ACM Transactions on Information and System
  Security, 4, (3), 224-274.
Sandhu, R., Coyne, E., Feinstein, H., and Youman, C. Role-based access
  control models. IEEE Computer, 29, (2), 38-47.
