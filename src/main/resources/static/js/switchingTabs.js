document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');
    const tabs = document.querySelectorAll('.bottom-box');
    const contents = document.querySelectorAll('.content-box');
    console.log('Tabs found:', tabs.length);
    console.log('Contents found:', contents.length);
    if (tabs.length && contents.length) {
        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                console.log('Tab clicked:', this.dataset.tab);
                tabs.forEach(t => t.classList.remove('active'));
                contents.forEach(c => c.classList.remove('active'));
                this.classList.add('active');
                const tabId = this.dataset.tab;
                const contentId = `${tabId}-content`;
                console.log('Looking for content:', contentId);
                document.getElementById(contentId).classList.add('active');
            });
        });
    }
});