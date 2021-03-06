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
myPort.postMessage({greeting_from_content_js: "hello from content script"});

myPort.onMessage.addListener(function(m) {
  // works, but should not be needed
  //document.getElementById('alert123').innerHTML = m.greeting;
  //myPort.postMessage({echo_port_msg_from_content_js: m["data"]});
  window.top.postMessage({from: "content", info: m["data"]}, '*');

  // does not work
  //console.log("content.js: myPort.onMessage");
});
