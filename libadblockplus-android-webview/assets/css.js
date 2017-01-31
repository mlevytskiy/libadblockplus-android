{
   {{DEBUG}} console.log('starting injecting css rules');
   {{BRIDGE}}.startGetElemhideSelectors();
   var selectorsCount = {{BRIDGE}}.getElemhideSelectorsCount();
   {{DEBUG}} console.log('got selectors: ' + selectorsCount);
   var head = document.getElementsByTagName("head")[0];
   var style = document.createElement("style");
   head.appendChild(style);
   var sheet = style.sheet ? style.sheet : style.styleSheet;
   for (var i = 0; i < selectorsCount; i++)
   {
     var selector = {{BRIDGE}}.getElemhideSelector(i);
     if (selector != undefined)
     {
       if (sheet.insertRule)
       {
         sheet.insertRule(selector + ' { display: none !important; }', 0);
       }
       else
       {
         sheet.addRule(selector, 'display: none !important;', 0);
       }
     }
   }
   {{BRIDGE}}.finishGetElemhideSelectors();
   {{DEBUG}} console.log('finished injecting css rules');
}