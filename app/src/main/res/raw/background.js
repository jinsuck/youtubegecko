let portFromCS;

function connectedPortContent(p) {
  portFromCS = p;
  //portFromCS.postMessage({greeting: "hi there content script!"});
  portFromCS.onMessage.addListener(function(m) {
    //portFromCS.postMessage({greeting: "In background script, received message from content script:" + m.greeting});
  });
}

browser.runtime.onConnect.addListener(connectedPortContent);








const nativePort = browser.runtime.connectNative("browser");

nativePort.onMessage.addListener(json => {
    nativePort.postMessage('echo background port received ' + json["code"]);

    // does not work :(
    // JavaScript Error: "Error: Could not establish connection. Receiving end does not exist.
    // browser.runtime.sendMessage({"from": "background", "data": json["code"]});


//    nativePort.postMessage('window top: ' + Object.keys(window.top));
//    nativePort.postMessage('chrome : ' + Object.keys(chrome.tabs));
//    nativePort.postMessage('browser : ' + Object.keys(browser.tabs.TabStatus));
//    nativePort.postMessage('browser runtime : ' + Object.keys(browser.runtime));
//    nativePort.postMessage('this keys: ' + Object.keys(this));

    // does not work :(
    // uncaught exception: Native app not found or this WebExtension does not have permissions.
    // chrome.tabs.executeScript({code:json['code']});

    // this works
    portFromCS.postMessage({greeting: json["code"]});
});

function onMessageReceivedFromContentScript(data) {
    if (data.from == "content") {
        nativePort.postMessage("from ContentScript " + data.data);
    }
}

browser.runtime.onMessage.addListener(onMessageReceivedFromContentScript)
//window.top.addEventListener('message', onMessageReceived, false);

//setTimeout(function() {nativePort.postMessage("Hello from WebExtension TIMEOUT 1000'!");}, 1000);


