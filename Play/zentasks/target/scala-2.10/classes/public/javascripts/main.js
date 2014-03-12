(function() {
  var AppRouter, Drawer, Group, Project, TaskFolder, TaskItem, Tasks, log,
    __slice = [].slice,
    __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  log = function() {
    var args;
    args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (console.log != null) {
      return console.log.apply(console, args);
    }
  };

  $(".options dt, .users dt").live("click", function(e) {
    e.preventDefault();
    if ($(e.target).parent().hasClass("opened")) {
      $(e.target).parent().removeClass("opened");
    } else {
      $(e.target).parent().addClass("opened");
      $(document).one("click", function() {
        return $(e.target).parent().removeClass("opened");
      });
    }
    return false;
  });

  $.fn.editInPlace = function() {
    var method, options;
    method = arguments[0], options = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    return this.each(function() {
      var methods;
      methods = {
        init: function(options) {
          var cancel, valid,
            _this = this;
          valid = function(e) {
            var newValue;
            newValue = _this.input.val();
            return options.onChange.call(options.context, newValue);
          };
          cancel = function(e) {
            _this.el.show();
            return _this.input.hide();
          };
          this.el = $(this).dblclick(methods.edit);
          return this.input = $("<input type='text' />").insertBefore(this.el).keyup(function(e) {
            switch (e.keyCode) {
              case 13:
                return $(this).blur();
              case 27:
                return cancel(e);
            }
          }).blur(valid).hide();
        },
        edit: function() {
          this.input.val(this.el.text()).show().focus().select();
          return this.el.hide();
        },
        close: function(newName) {
          this.el.text(newName).show();
          return this.input.hide();
        }
      };
      if (methods[method]) {
        return methods[method].apply(this, options);
      } else if (typeof method === 'object') {
        return methods.init.call(this, method);
      } else {
        return $.error("Method " + method + " does not exist.");
      }
    });
  };

  Drawer = (function(_super) {

    __extends(Drawer, _super);

    function Drawer() {
      return Drawer.__super__.constructor.apply(this, arguments);
    }

    Drawer.prototype.initialize = function() {
      $("#newGroup").click(this.addGroup);
      return this.el.children("li").each(function(i, group) {
        new Group({
          el: $(group)
        });
        return $("li", group).each(function(i, project) {
          return new Project({
            el: $(project)
          });
        });
      });
    };

    Drawer.prototype.addGroup = function() {
      var r;
      r = jsRoutes.controllers.Projects.addGroup();
      return $.ajax({
        url: r.url,
        type: r.type,
        success: function(data) {
          var _view;
          _view = new Group({
            el: $(data).appendTo("#projects")
          });
          return _view.el.find(".groupName").editInPlace("edit");
        },
        error: function(err) {}
      });
    };

    return Drawer;

  })(Backbone.View);

  Group = (function(_super) {

    __extends(Group, _super);

    function Group() {
      this.renameGroup = __bind(this.renameGroup, this);
      return Group.__super__.constructor.apply(this, arguments);
    }

    Group.prototype.events = {
      "click    .toggle": "toggle",
      "click    .newProject": "newProject",
      "click    .deleteGroup": "deleteGroup"
    };

    Group.prototype.initialize = function() {
      this.id = this.el.attr("data-group");
      return this.name = $(".groupName", this.el).editInPlace({
        context: this,
        onChange: this.renameGroup
      });
    };

    Group.prototype.newProject = function(e) {
      var r;
      e.preventDefault();
      this.el.removeClass("closed");
      r = jsRoutes.controllers.Projects.add();
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          group: this.el.attr("data-group")
        },
        success: function(tpl) {
          var _list, _view;
          _list = $("ul", this.el);
          _view = new Project({
            el: $(tpl).appendTo(_list)
          });
          return _view.el.find(".name").editInPlace("edit");
        },
        error: function(err) {
          return $.error("Error: " + err);
        }
      });
    };

    Group.prototype.deleteGroup = function(e) {
      var id, r;
      e.preventDefault();
      if (!confirm("Remove group and projects inside?")) {
        false;
      }
      id = this.el.attr("data-group-id");
      this.loading(true);
      r = jsRoutes.controllers.Projects.deleteGroup(this.id);
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        success: function() {
          this.el.remove();
          return this.loading(false);
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
    };

    Group.prototype.renameGroup = function(name) {
      var r;
      this.loading(true);
      r = jsRoutes.controllers.Projects.renameGroup(this.id);
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          name: name
        },
        success: function(data) {
          this.loading(false);
          this.name.editInPlace("close", data);
          this.el.attr("data-group", data);
          return this.id = this.el.attr("data-group");
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
    };

    Group.prototype.toggle = function(e) {
      e.preventDefault();
      this.el.toggleClass("closed");
      return false;
    };

    Group.prototype.loading = function(display) {
      if (display) {
        this.el.children(".options").hide();
        return this.el.children(".loader").show();
      } else {
        this.el.children(".options").show();
        return this.el.children(".loader").hide();
      }
    };

    return Group;

  })(Backbone.View);

  Project = (function(_super) {

    __extends(Project, _super);

    function Project() {
      return Project.__super__.constructor.apply(this, arguments);
    }

    Project.prototype.events = {
      "click      .delete": "deleteProject"
    };

    Project.prototype.initialize = function() {
      this.id = this.el.attr("data-project");
      return this.name = $(".name", this.el).editInPlace({
        context: this,
        onChange: this.renameProject
      });
    };

    Project.prototype.renameProject = function(name) {
      var r;
      this.loading(true);
      r = jsRoutes.controllers.Projects.rename(this.id);
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          name: name
        },
        success: function(data) {
          this.loading(false);
          return this.name.editInPlace("close", data);
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
    };

    Project.prototype.deleteProject = function(e) {
      var r;
      e.preventDefault();
      this.loading(true);
      r = jsRoutes.controllers.Projects["delete"](this.id);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        success: function() {
          this.el.remove();
          return this.loading(false);
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
      return false;
    };

    Project.prototype.loading = function(display) {
      if (display) {
        this.el.children(".delete").hide();
        return this.el.children(".loader").show();
      } else {
        this.el.children(".delete").show();
        return this.el.children(".loader").hide();
      }
    };

    return Project;

  })(Backbone.View);

  AppRouter = (function(_super) {

    __extends(AppRouter, _super);

    function AppRouter() {
      return AppRouter.__super__.constructor.apply(this, arguments);
    }

    AppRouter.prototype.initialize = function() {
      return this.currentApp = new Tasks({
        el: $("#main")
      });
    };

    AppRouter.prototype.routes = {
      "/": "index",
      "/projects/:project/tasks": "tasks"
    };

    AppRouter.prototype.index = function() {
      return $("#main").load("/ #main");
    };

    AppRouter.prototype.tasks = function(project) {
      var currentApp;
      currentApp = this.currentApp;
      return $("#main").load("/projects/" + project + "/tasks", function(tpl) {
        return currentApp.render(project);
      });
    };

    return AppRouter;

  })(Backbone.Router);

  Tasks = (function(_super) {

    __extends(Tasks, _super);

    function Tasks() {
      return Tasks.__super__.constructor.apply(this, arguments);
    }

    Tasks.prototype.events = {
      "click .newFolder": "newFolder",
      "click .list .action": "removeUser",
      "click .addUserList .action": "addUser"
    };

    Tasks.prototype.render = function(project) {
      var _this = this;
      this.project = project;
      return this.folders = $.map($(".folder", this.el), function(folder) {
        return new TaskFolder({
          el: $(folder),
          project: _this.project
        });
      });
    };

    Tasks.prototype.newFolder = function(e) {
      var r;
      e.preventDefault();
      r = jsRoutes.controllers.Tasks.addFolder(this.project);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        success: function(tpl) {
          var newFolder;
          newFolder = new TaskFolder({
            el: $(tpl).insertBefore(".newFolder"),
            project: this.project
          });
          return newFolder.el.find("header > h3").editInPlace("edit");
        },
        error: function(err) {
          return $.error("Error: " + err);
        }
      });
      return false;
    };

    Tasks.prototype.removeUser = function(e) {
      var r;
      e.preventDefault();
      r = jsRoutes.controllers.Projects.removeUser(this.project);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          user: $(e.target).parent().data('user-id')
        },
        success: function() {
          return $(e.target).parent().appendTo(".addUserList");
        },
        error: function(err) {
          return $.error("Error: " + err);
        }
      });
      return false;
    };

    Tasks.prototype.addUser = function(e) {
      var r;
      e.preventDefault();
      r = jsRoutes.controllers.Projects.addUser(this.project);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          user: $(e.target).parent().data('user-id')
        },
        success: function() {
          return $(e.target).parent().appendTo(".users .list");
        },
        error: function(err) {
          return $.error("Error: " + err);
        }
      });
      return false;
    };

    return Tasks;

  })(Backbone.View);

  TaskFolder = (function(_super) {

    __extends(TaskFolder, _super);

    function TaskFolder() {
      this.deleteTask = __bind(this.deleteTask, this);

      this.refreshCount = __bind(this.refreshCount, this);

      this.toggleAll = __bind(this.toggleAll, this);

      this.deleteFolder = __bind(this.deleteFolder, this);

      this.deleteAllTasks = __bind(this.deleteAllTasks, this);

      this.deleteCompleteTasks = __bind(this.deleteCompleteTasks, this);

      this.renameFolder = __bind(this.renameFolder, this);

      this.newTask = __bind(this.newTask, this);

      this.initialize = __bind(this.initialize, this);
      return TaskFolder.__super__.constructor.apply(this, arguments);
    }

    TaskFolder.prototype.events = {
      "click .deleteCompleteTasks": "deleteCompleteTasks",
      "click .deleteAllTasks": "deleteAllTasks",
      "click .deleteFolder": "deleteFolder",
      "change header>input": "toggleAll",
      "submit .addTask": "newTask"
    };

    TaskFolder.prototype.initialize = function(options) {
      var _this = this;
      this.project = options.project;
      this.tasks = $.map($(".list li", this.el), function(item) {
        var newTask;
        newTask = new TaskItem({
          el: $(item),
          folder: _this
        });
        newTask.bind("change", _this.refreshCount);
        return newTask.bind("delete", _this.deleteTask);
      });
      this.counter = this.el.find(".counter");
      this.id = this.el.attr("data-folder-id");
      this.name = $("header > h3", this.el).editInPlace({
        context: this,
        onChange: this.renameFolder
      });
      return this.refreshCount();
    };

    TaskFolder.prototype.newTask = function(e) {
      var form, r, taskBody;
      e.preventDefault();
      $(document).focus();
      form = $(e.target);
      taskBody = $("input[name=taskBody]", form).val();
      r = jsRoutes.controllers.Tasks.add(this.project, this.id);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          title: $("input[name=taskBody]", form).val(),
          dueDate: $("input[name=dueDate]", form).val(),
          assignedTo: {
            email: $("input[name=assignedTo]", form).val()
          }
        },
        success: function(tpl) {
          var newTask;
          newTask = new TaskItem({
            el: $(tpl),
            folder: this
          });
          this.el.find("ul").append(newTask.el);
          this.tasks.push(newTask);
          return form.find("input[type=text]").val("").first().focus();
        },
        error: function(err) {
          return alert("Something went wrong:" + err);
        }
      });
      return false;
    };

    TaskFolder.prototype.renameFolder = function(name) {
      var r;
      this.loading(true);
      r = jsRoutes.controllers.Tasks.renameFolder(this.project, this.id);
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          name: name
        },
        success: function(data) {
          this.loading(false);
          this.name.editInPlace("close", data);
          this.el.attr("data-folder-id", data);
          return this.id = this.el.attr("data-folder-id");
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
    };

    TaskFolder.prototype.deleteCompleteTasks = function(e) {
      e.preventDefault();
      $.each(this.tasks, function(i, item) {
        if (item.el.find(".done:checked").length > 0) {
          item.deleteTask();
        }
        return true;
      });
      return false;
    };

    TaskFolder.prototype.deleteAllTasks = function(e) {
      e.preventDefault();
      $.each(this.tasks, function(i, item) {
        item.deleteTask();
        return true;
      });
      return false;
    };

    TaskFolder.prototype.deleteFolder = function(e) {
      e.preventDefault();
      this.el.remove();
      return false;
    };

    TaskFolder.prototype.toggleAll = function(e) {
      var val;
      val = $(e.target).is(":checked");
      return $.each(this.tasks, function(i, item) {
        item.toggle(val);
        return true;
      });
    };

    TaskFolder.prototype.refreshCount = function() {
      var count;
      count = this.tasks.filter(function(item) {
        return item.el.find(".done:checked").length === 0;
      }).length;
      return this.counter.text(count);
    };

    TaskFolder.prototype.deleteTask = function(task) {
      this.tasks = _.without(this.tasks, tasks);
      return this.refreshCount();
    };

    TaskFolder.prototype.loading = function(display) {
      if (display) {
        this.el.find("header .options").hide();
        return this.el.find("header .loader").show();
      } else {
        this.el.find("header .options").show();
        return this.el.find("header .loader").hide();
      }
    };

    return TaskFolder;

  })(Backbone.View);

  TaskItem = (function(_super) {

    __extends(TaskItem, _super);

    function TaskItem() {
      this.onToggle = __bind(this.onToggle, this);

      this.toggle = __bind(this.toggle, this);

      this.editTask = __bind(this.editTask, this);

      this.deleteTask = __bind(this.deleteTask, this);
      return TaskItem.__super__.constructor.apply(this, arguments);
    }

    TaskItem.prototype.events = {
      "change .done": "onToggle",
      "click .deleteTask": "deleteTask",
      "dblclick h4": "editTask"
    };

    TaskItem.prototype.initialize = function(options) {
      this.check = this.el.find(".done");
      this.id = this.el.attr("data-task-id");
      return this.folder = options.folder;
    };

    TaskItem.prototype.deleteTask = function(e) {
      var r;
      if (e != null) {
        e.preventDefault();
      }
      this.loading(false);
      r = jsRoutes.controllers.Tasks["delete"](this.id);
      $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          name: name
        },
        success: function(data) {
          this.loading(false);
          this.el.remove();
          return this.trigger("delete", this);
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
      return false;
    };

    TaskItem.prototype.editTask = function(e) {
      e.preventDefault();
      alert("not implemented yet.");
      return false;
    };

    TaskItem.prototype.toggle = function(val) {
      var r;
      this.loading(true);
      r = jsRoutes.controllers.Tasks.update(this.id);
      return $.ajax({
        url: r.url,
        type: r.type,
        context: this,
        data: {
          done: val
        },
        success: function(data) {
          this.loading(false);
          this.check.attr("checked", val);
          return this.trigger("change", this);
        },
        error: function(err) {
          this.loading(false);
          return $.error("Error: " + err);
        }
      });
    };

    TaskItem.prototype.onToggle = function(e) {
      var val;
      e.preventDefault();
      val = this.check.is(":checked");
      log(val);
      this.toggle(val);
      return false;
    };

    TaskItem.prototype.loading = function(display) {
      if (display) {
        this.el.find(".delete").hide();
        return this.el.find(".loader").show();
      } else {
        this.el.find(".delete").show();
        return this.el.find(".loader").hide();
      }
    };

    return TaskItem;

  })(Backbone.View);

  $(function() {
    var app, drawer;
    app = new AppRouter();
    drawer = new Drawer({
      el: $("#projects")
    });
    return Backbone.history.start({
      pushHistory: true
    });
  });

}).call(this);
