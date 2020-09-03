function onMessage(event) {
    //browser.runtime.sendNativeMessage('messaging@imvu.com', event.data);
    browser.runtime.sendNativeMessage('messaging@imvu.com', "something something");
    browser.runtime.sendNativeMessage("browser", "something something");
}

window.top.addEventListener('message', onMessage, false);

browser.runtime.sendNativeMessage("browser", "something something NOW 1");
browser.runtime.sendNativeMessage("messaging@imvu.com", "something something NOW 2");