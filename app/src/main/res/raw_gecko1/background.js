// Establish connection with app
let nativePort = browser.runtime.connectNative("browser");

nativePort.onMessage.addListener(json => {
    chrome.tabs.executeScript({code:json['code']});
});

// handling messages from content script
function onMessageReceived(message) {
    // redirect it to native application
    nativePort.postMessage(message);
}

browser.runtime.onMessage.addListener(onMessageReceived)
