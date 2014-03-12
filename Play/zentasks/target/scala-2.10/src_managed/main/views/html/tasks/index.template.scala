
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
object index extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[Project,List[Task],play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(project: Project, tasks: List[Task]):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.39*/("""

<header>
    <hgroup>
        <h1>"""),_display_(Seq[Any](/*5.14*/project/*5.21*/.folder)),format.raw/*5.28*/("""</h1>
        <h2>"""),_display_(Seq[Any](/*6.14*/project/*6.21*/.name)),format.raw/*6.26*/("""</h2>
    </hgroup>
    <dl class="users">
        <dt>Project's team</dt>
        <dd>
            <div class="wrap">
                <h3>Team mates</h3>
                <div class="list">
                    """),_display_(Seq[Any](/*14.22*/project/*14.29*/.members.map/*14.41*/ { user =>_display_(Seq[Any](format.raw/*14.51*/("""
                        <dl data-user-id=""""),_display_(Seq[Any](/*15.44*/user/*15.48*/.email)),format.raw/*15.54*/("""">
                            <dt>"""),_display_(Seq[Any](/*16.34*/user/*16.38*/.name)),format.raw/*16.43*/(""" <span>("""),_display_(Seq[Any](/*16.52*/user/*16.56*/.email)),format.raw/*16.62*/(""")</span></dt>
                            <dd class="action">Action</dd>
                        </dl>
                    """)))})),format.raw/*19.22*/("""
                </div>
                <h3>Add a team mate</h3>
                <div class="addUserList">
                    """),_display_(Seq[Any](/*23.22*/User/*23.26*/.findAll.diff(project.members).map/*23.60*/ { user =>_display_(Seq[Any](format.raw/*23.70*/("""
                        <dl data-user-id=""""),_display_(Seq[Any](/*24.44*/user/*24.48*/.email)),format.raw/*24.54*/("""">
                            <dt>"""),_display_(Seq[Any](/*25.34*/user/*25.38*/.name)),format.raw/*25.43*/(""" <span>("""),_display_(Seq[Any](/*25.52*/user/*25.56*/.email)),format.raw/*25.62*/(""")</span></dt>
                            <dd class="action">Action</dd>
                        </dl>
                    """)))})),format.raw/*28.22*/("""
                </div>
            </div>
        </dd>
    </dl>
</header>
<article  class="tasks" id="tasks">
    """),_display_(Seq[Any](/*35.6*/tasks/*35.11*/.groupBy(_.folder).map/*35.33*/ {/*36.9*/case (folder, tasks) =>/*36.32*/ {_display_(Seq[Any](format.raw/*36.34*/("""
            """),_display_(Seq[Any](/*37.14*/views/*37.19*/.html.tasks.folder(folder, tasks))),format.raw/*37.52*/("""
        """)))}})),format.raw/*39.6*/("""
    <a href="#newFolder" class="new newFolder">New folder</a>
</article>

"""))}
    }
    
    def render(project:Project,tasks:List[Task]): play.api.templates.HtmlFormat.Appendable = apply(project,tasks)
    
    def f:((Project,List[Task]) => play.api.templates.HtmlFormat.Appendable) = (project,tasks) => apply(project,tasks)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Sun Mar 09 23:36:48 CET 2014
                    SOURCE: /home/math/Play/zentasks/app/views/tasks/index.scala.html
                    HASH: fff16d08afe61c94cef0c664d750bb3c2b1f82b2
                    MATRIX: 792->1|923->38|995->75|1010->82|1038->89|1092->108|1107->115|1133->120|1380->331|1396->338|1417->350|1465->360|1545->404|1558->408|1586->414|1658->450|1671->454|1698->459|1743->468|1756->472|1784->478|1940->602|2104->730|2117->734|2160->768|2208->778|2288->822|2301->826|2329->832|2401->868|2414->872|2441->877|2486->886|2499->890|2527->896|2683->1020|2836->1138|2850->1143|2881->1165|2891->1176|2923->1199|2963->1201|3013->1215|3027->1220|3082->1253|3124->1269
                    LINES: 26->1|29->1|33->5|33->5|33->5|34->6|34->6|34->6|42->14|42->14|42->14|42->14|43->15|43->15|43->15|44->16|44->16|44->16|44->16|44->16|44->16|47->19|51->23|51->23|51->23|51->23|52->24|52->24|52->24|53->25|53->25|53->25|53->25|53->25|53->25|56->28|63->35|63->35|63->35|63->36|63->36|63->36|64->37|64->37|64->37|65->39
                    -- GENERATED --
                */
            