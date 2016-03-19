This project implements the NIST model of role-based access control, a form of access control in computer security that indirectly assigns access permissions to users based on their "roles" in the system (which can be likened to their jobs in an organization, or their authority). Included is a file server and client program for reading and modifying files on the server, though appropriate permissions for the user must be granted before they can do any reading/writing.
Though I put a working build on Github back in April/May 2015, I am in the process of rewriting it, in order to make the code more polished and bug-free and to support a variety of new features.

The project will use a Java client and server with SQLite. The previous version utilized a Microsoft SQL Server 2008-R2 backend, with the Microsoft JDBC driver for SQL Server (ver. 4.1).

For more information on role-based access control, see the following papers (The latter introduced the idea of RBAC in 1996, and the former proposed a NIST standard for it in 2001):

Ferraiolo, D.F., Sandhu, R.S., and Gavrila, S. Proposed NIST standard for role-based access control. ACM Transactions on Information and System Security, 4, (3), 224-274.

Sandhu, R., Coyne, E., Feinstein, H., and Youman, C. Role-based access control models. IEEE Computer, 29, (2), 38-47.
