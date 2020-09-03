// Establish connection with app
const nativePort = browser.runtime.connectNative("browser");

nativePort.onMessage.addListener(json => {
    //chrome.tabs.executeScript({code:json['code']});
    nativePort.postMessage('Received: ${JSON.stringify(json)}');
});

// handling messages from content script
function onMessageReceived(message) {
    // redirect it to native application
    nativePort.postMessage(message);
}

browser.runtime.onMessage.addListener(onMessageReceived)

nativePort.postMessage("Hello from web");
//setTimeout(function() {nativePort.postMessage("Hello from WebExtension TIMEOUT 1000'!");}, 1000);