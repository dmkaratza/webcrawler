package com.leonteq.webcrawler.samples

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object WikiHtml {

  def sample(): Document = {
    Jsoup.parse(
      """https://fr.wikipedia.org/wiki/Akka_(logiciel)
        |<!doctype html>
        |<html class="client-nojs" lang="en" dir="ltr">
        | <head>
        |  <meta charset="UTF-8">
        |  <title>Akka (toolkit) - Wikipedia</title>
        |  <link rel="stylesheet" href="/w/load.php?lang=en&amp;modules=ext.cite.styles%7Cext.uls.interlanguage%7Cext.visualEditor.desktopArticleTarget.noscript%7Cext.wikimediaBadges%7Cskins.vector.styles.legacy%7Cwikibase.client.init&amp;only=styles&amp;skin=vector">
        |  <script async src="/w/load.php?lang=en&amp;modules=startup&amp;only=scripts&amp;raw=1&amp;skin=vector"></script>
        |  <meta name="ResourceLoaderDynamicStyles" content="">
        |  <link rel="stylesheet" href="/w/load.php?lang=en&amp;modules=site.styles&amp;only=styles&amp;skin=vector">
        |  <link rel="license" href="//creativecommons.org/licenses/by-sa/3.0/">
        |  <link rel="canonical" href="https://en.wikipedia.org/wiki/Akka_(toolkit)">
        |  <link rel="dns-prefetch" href="//login.wikimedia.org">
        |  <link rel="dns-prefetch" href="//meta.wikimedia.org">
        | </head>
        | <body class="mediawiki ltr sitedir-ltr mw-hide-empty-elt ns-0 ns-subject mw-editable page-Akka_toolkit rootpage-Akka_toolkit skin-vector action-view skin-vector-legacy">
        |  <div id="mw-navigation">
        |   <h2>Navigation menu</h2>
        |   <div id="mw-head">
        |    <nav id="p-personal" class="mw-portlet mw-portlet-personal vector-user-menu-legacy vector-menu" aria-labelledby="p-personal-label" role="navigation">
        |     <h3 id="p-personal-label" class="vector-menu-heading"> <span>Personal tools</span> </h3>
        |     <div class="vector-menu-content">
        |      <ul class="vector-menu-content-list">
        |       <li id="pt-login"><a href="/w/index.php?title=Special:UserLogin&amp;returnto=Akka+%28toolkit%29" title="You're encouraged to log in; however, it's not mandatory. [o]" accesskey="o">Log in</a></li>
        |      </ul>
        |     </div>
        |    </nav>
        |   <div id="mw-panel">
        |    <nav id="p-lang" class="mw-portlet mw-portlet-lang vector-menu vector-menu-portal portal" aria-labelledby="p-lang-label" role="navigation">
        |     <h3 id="p-lang-label" class="vector-menu-heading"> <span>Languages</span> </h3>
        |     <div class="vector-menu-content">
        |      <ul class="vector-menu-content-list">
        |       <li class="interlanguage-link interwiki-fr"><a href="https://fr.wikipedia.org/wiki/Akka_(logiciel)" title="Akka (logiciel) – French" lang="fr" hreflang="fr" class="interlanguage-link-target">Français</a></li>
        |       <li class="interlanguage-link interwiki-ko"><a href="https://ko.wikipedia.org/wiki/Akka" title="Akka – Korean" lang="ko" hreflang="ko" class="interlanguage-link-target">한국어</a></li>
        |       <li class="interlanguage-link interwiki-zh"><a href="https://zh.wikipedia.org/wiki/Akka" title="Akka – Chinese" lang="zh" hreflang="zh" class="interlanguage-link-target">中文</a></li>
        |      </ul>
        |      <div class="after-portlet after-portlet-lang">
        |       <span class="wb-langlinks-edit wb-langlinks-link"><a href="https://www.wikidata.org/wiki/Special:EntityPage/Q16002307#sitelinks-wikipedia" title="Edit interlanguage links" class="wbc-editpage">Edit links</a></span>
        |      </div>
        |     </div>
        |    </nav>
        |   </div>
        |  </div>
        |  <footer id="footer" class="mw-footer" role="contentinfo">
        |   <ul id="footer-places">
        |    <li id="footer-places-developers"><a href="https://www.mediawiki.org/wiki/Special:MyLanguage/How_to_contribute">Developers</a></li>
        |    <li id="footer-places-statslink"><a href="https://stats.wikimedia.org/#/en.wikipedia.org">Statistics</a></li>
        |    <li id="footer-places-cookiestatement"><a href="https://foundation.wikimedia.org/wiki/Cookie_statement">Cookie statement</a></li>
        |   </ul>
        |   <ul id="footer-icons" class="noprint">
        |    <li id="footer-copyrightico"><a href="https://wikimediafoundation.org/"><img src="/static/images/footer/wikimedia-button.png" srcset="/static/images/footer/wikimedia-button-1.5x.png 1.5x, /static/images/footer/wikimedia-button-2x.png 2x" width="88" height="31" alt="Wikimedia Foundation" loading="lazy"></a></li>
        |    <li id="footer-poweredbyico"><a href="https://www.mediawiki.org/"><img src="/static/images/footer/poweredby_mediawiki_88x31.png" alt="Powered by MediaWiki" srcset="/static/images/footer/poweredby_mediawiki_132x47.png 1.5x, /static/images/footer/poweredby_mediawiki_176x62.png 2x" width="88" height="31" loading="lazy"></a></li>
        |   </ul>
        |  </footer>
        | </body>
        |</html>
        |""".stripMargin)
  }
}
