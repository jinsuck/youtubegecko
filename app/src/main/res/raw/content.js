function onMessageFromPage(event) {
    if (event.data.from == "page") {
        browser.runtime.sendMessage({"from": "content", "data": "onMessageFromPage: " + event.data.info});
    }
}

window.top.addEventListener('message', onMessageFromPage, false);


// does not work
//function onMessageReceivedFromBackgroundScript(data) {
//    if (data.from == "background") {
//        window.top.postMessage({'from' : 'content', 'info': data.data}, '*');
//    }
//}
//browser.runtime.onMessage.addListener(onMessageReceivedFromBackgroundScript)


let myPort = browser.runtime.connect({name:"port-from-content"});
myPort.postMessage({greeting: "hello from content script"});

myPort.onMessage.addListener(function(m) {
  // works, but should not be needed
  //document.getElementById('alert123').innerHTML = m.greeting;
  window.top.postMessage({from : 'content', info: m.greeting}, '*');

  // does not work
  //console.log("content.js: myPort.onMessage");
});
