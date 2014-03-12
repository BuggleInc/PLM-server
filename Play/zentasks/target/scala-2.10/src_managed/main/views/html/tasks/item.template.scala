
package views.html.tasks

import play.templates._
import play.templates.TemplateMagic._

import play.api.templates._
import play.api.templates.PlayMagic._
import models._
import controllers._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.api.i18n._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import views.html._
/**/
object item extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[Task,Boolean,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(task: Task, isEditable: Boolean = true):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.42*/("""

<li data-task-id=""""),_display_(Seq[Any](/*3.20*/task/*3.24*/.id)),format.raw/*3.27*/("""">
    
    """),_display_(Seq[Any](/*5.6*/if(isEditable)/*5.20*/ {_display_(Seq[Any](format.raw/*5.22*/("""
        <input class="done" type="checkbox" """),_display_(Seq[Any](/*6.46*/(if(task.done) "checked"))),format.raw/*6.71*/(""" />
    """)))})),format.raw/*7.6*/("""
    
    <h4>"""),_display_(Seq[Any](/*9.10*/task/*9.14*/.title)),format.raw/*9.20*/("""</h4>
    
    """),_display_(Seq[Any](/*11.6*/if(task.dueDate != null)/*11.30*/ {_display_(Seq[Any](format.raw/*11.32*/("""
        <time datetime=""""),_display_(Seq[Any](/*12.26*/task/*12.30*/.dueDate)),format.raw/*12.38*/("""">"""),_display_(Seq[Any](/*12.41*/task/*12.45*/.dueDate.format("MMM dd yyyy"))),format.raw/*12.75*/("""</time>
    """)))})),format.raw/*13.6*/("""
    
    """),_display_(Seq[Any](/*15.6*/if(task.assignedTo != null && task.assignedTo.email != null)/*15.66*/ {_display_(Seq[Any](format.raw/*15.68*/("""
        <span class="assignedTo">"""),_display_(Seq[Any](/*16.35*/task/*16.39*/.assignedTo.email)),format.raw/*16.56*/("""</span>
    """)))})),format.raw/*17.6*/("""
    
    """),_display_(Seq[Any](/*19.6*/if(isEditable)/*19.20*/ {_display_(Seq[Any](format.raw/*19.22*/("""
        <a class="deleteTask" href=""""),_display_(Seq[Any](/*20.38*/routes/*20.44*/.Tasks.delete(task.id))),format.raw/*20.66*/("""">Delete task</a>
        <span class="loader">Loading</span>
    """)))})),format.raw/*22.6*/("""
    
</li>
"""))}
    }
    
    def render(task:Task,isEditable:Boolean): play.api.templates.HtmlFormat.Appendable = apply(task,isEditable)
    
    def f:((Task,Boolean) => play.api.templates.HtmlFormat.Appendable) = (task,isEditable) => apply(task,isEditable)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Sun Mar 09 23:36:48 CET 2014
                    SOURCE: /home/math/Play/zentasks/app/views/tasks/item.scala.html
                    HASH: b7a2187e7e6f54fbfda8f75335bc49fcb69da459
                    MATRIX: 785->1|919->41|975->62|987->66|1011->69|1058->82|1080->96|1119->98|1200->144|1246->169|1285->178|1335->193|1347->197|1374->203|1425->219|1458->243|1498->245|1560->271|1573->275|1603->283|1642->286|1655->290|1707->320|1751->333|1797->344|1866->404|1906->406|1977->441|1990->445|2029->462|2073->475|2119->486|2142->500|2182->502|2256->540|2271->546|2315->568|2413->635
                    LINES: 26->1|29->1|31->3|31->3|31->3|33->5|33->5|33->5|34->6|34->6|35->7|37->9|37->9|37->9|39->11|39->11|39->11|40->12|40->12|40->12|40->12|40->12|40->12|41->13|43->15|43->15|43->15|44->16|44->16|44->16|45->17|47->19|47->19|47->19|48->20|48->20|48->20|50->22
                    -- GENERATED --
                */
            