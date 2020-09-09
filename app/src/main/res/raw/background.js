const nativePort = browser.runtime.connectNative("browser");

let portFromCS;

function connectedPortContent(p) {
  nativePort.postMessage("connectedPortContent");
  portFromCS = p;
  portFromCS.onMessage.addListener(function(m) {
      nativePort.postMessage("onMessage " + Object.keys(m));
  });
}

browser.runtime.onConnect.addListener(connectedPortContent);









nativePort.onMessage.addListener(jsonObj => {
    nativePort.postMessage('echo background port received ' + jsonObj["code"]);
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
    portFromCS.postMessage({from: "background", data: jsonObj["code"]});
});

function onMessageReceivedFromContentScript(data) {
    if (data.from == "content") {
        nativePort.postMessage("from ContentScript " + data.data);
    }
}

browser.runtime.onMessage.addListener(onMessageReceivedFromContentScript)
//window.top.addEventListener('message', onMessageReceived, false);

//setTimeout(function() {nativePort.postMessage("Hello from WebExtension TIMEOUT 1000'!");}, 1000);


