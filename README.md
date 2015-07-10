PLM-server
==========

Web server to navigate the code written by PLM users.

This is an early prototype, be careful.

Getting Started
---------------

- [Install Play Framework](https://www.playframework.com/documentation/2.3.x/Installing).
- Run ```activator ~run``` from the git checkout
- Launch a browser to <https://localhost:9000>
- You will get a big red warning windows stating that you have to fix
  your database. Click on the little "Apply this script now!" button
  near the top of the page. You may need to insist by clicking on
  "Mark this as resolved".

You should now have the main window running. The two first buttons are
known to not work since we lost the ability to register students to a
given course since then. 

Refreshing the students' data
-----------------------------

The first time you click on the "See all students" button, the server
will fetch all the data from the https://github.com/BuggleInc/PLM-data
repository (this may take a few minutes).

Then, the only way to to update the data is to do so manually:
```
cd repo/
git fetch --all
```

License
-------

Copyright 2015 INRIA

WebPLM is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

WebPLM is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with the source code.  If not, see <http://www.gnu.org/licenses/>.
