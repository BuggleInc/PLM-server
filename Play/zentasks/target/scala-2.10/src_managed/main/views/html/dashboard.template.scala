
package views.html

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
object dashboard extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template3[List[Project],List[Task],User,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(projects: List[Project], todoTasks: List[Task], user: User):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.62*/("""

"""),_display_(Seq[Any](/*3.2*/main(projects, user)/*3.22*/{_display_(Seq[Any](format.raw/*3.23*/("""
    
    <header>
        <hgroup>
            <h1>Dashboard</h1>
            <h2>Tasks over all projects</h2>
        </hgroup>
    </header>
    
    <article  class="tasks">
        """),_display_(Seq[Any](/*13.10*/todoTasks/*13.19*/.groupBy(_.project).map/*13.42*/ {/*14.13*/case (project, tasks) =>/*14.37*/ {_display_(Seq[Any](format.raw/*14.39*/("""
                <div class="folder" data-folder-id=""""),_display_(Seq[Any](/*15.54*/project/*15.61*/.id)),format.raw/*15.64*/("""">
                    <header>
                        <h3><a href="#"""),_display_(Seq[Any](/*17.40*/routes/*17.46*/.Tasks.index(project.id))),format.raw/*17.70*/("""">"""),_display_(Seq[Any](/*17.73*/project/*17.80*/.name)),format.raw/*17.85*/("""</a></h3>
                        <span class="loader">Loading</span>
                    </header>
                    <ul class="list">
                        """),_display_(Seq[Any](/*21.26*/tasks/*21.31*/.map/*21.35*/ { task =>_display_(Seq[Any](format.raw/*21.45*/("""
                            """),_display_(Seq[Any](/*22.30*/views/*22.35*/.html.tasks.item(task, isEditable = false))),format.raw/*22.77*/("""
                        """)))})),format.raw/*23.26*/("""
                    </ul>
                </div>
            """)))}})),format.raw/*27.10*/("""
    </article>
    
""")))})),format.raw/*30.2*/("""

"""))}
    }
    
    def render(projects:List[Project],todoTasks:List[Task],user:User): play.api.templates.HtmlFormat.Appendable = apply(projects,todoTasks,user)
    
    def f:((List[Project],List[Task],User) => play.api.templates.HtmlFormat.Appendable) = (projects,todoTasks,user) => apply(projects,todoTasks,user)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Sun Mar 09 23:36:48 CET 2014
                    SOURCE: /home/math/Play/zentasks/app/views/dashboard.scala.html
                    HASH: e78069843cc7f04c6a2613331bd327993f7f5cbe
                    MATRIX: 801->1|955->61|992->64|1020->84|1058->85|1281->272|1299->281|1331->304|1342->319|1375->343|1415->345|1505->399|1521->406|1546->409|1653->480|1668->486|1714->510|1753->513|1769->520|1796->525|1995->688|2009->693|2022->697|2070->707|2136->737|2150->742|2214->784|2272->810|2368->883|2421->905
                    LINES: 26->1|29->1|31->3|31->3|31->3|41->13|41->13|41->13|41->14|41->14|41->14|42->15|42->15|42->15|44->17|44->17|44->17|44->17|44->17|44->17|48->21|48->21|48->21|48->21|49->22|49->22|49->22|50->23|53->27|56->30
                    -- GENERATED --
                */
            