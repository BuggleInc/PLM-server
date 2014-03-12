
package views.html.projects

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
object item extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template1[Project,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(project: Project):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.20*/("""

<li data-project=""""),_display_(Seq[Any](/*3.20*/project/*3.27*/.id)),format.raw/*3.30*/("""">
    <a class="name" href="#"""),_display_(Seq[Any](/*4.29*/routes/*4.35*/.Tasks.index(project.id))),format.raw/*4.59*/("""">"""),_display_(Seq[Any](/*4.62*/project/*4.69*/.name)),format.raw/*4.74*/("""</a>
    <button class="delete" href=""""),_display_(Seq[Any](/*5.35*/routes/*5.41*/.Projects.delete(project.id))),format.raw/*5.69*/("""">Delete</button>
    <span class="loader">Loading</span>
</li>
"""))}
    }
    
    def render(project:Project): play.api.templates.HtmlFormat.Appendable = apply(project)
    
    def f:((Project) => play.api.templates.HtmlFormat.Appendable) = (project) => apply(project)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Sun Mar 09 23:36:48 CET 2014
                    SOURCE: /home/math/Play/zentasks/app/views/projects/item.scala.html
                    HASH: 5d27917c23b17c7c5a39b52e0b37abd6f26976d8
                    MATRIX: 783->1|895->19|951->40|966->47|990->50|1056->81|1070->87|1115->111|1153->114|1168->121|1194->126|1268->165|1282->171|1331->199
                    LINES: 26->1|29->1|31->3|31->3|31->3|32->4|32->4|32->4|32->4|32->4|32->4|33->5|33->5|33->5
                    -- GENERATED --
                */
            