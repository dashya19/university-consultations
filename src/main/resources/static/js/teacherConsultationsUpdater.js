document.addEventListener('DOMContentLoaded', function() {
    let lastUpdateTime = 0;
    const updateInterval = 5 * 60 * 1000;
    function checkConsultationsUpdate() {
        const currentTime = new Date().getTime();
        if (currentTime - lastUpdateTime > updateInterval) {
            fetch('/teacher/update-consultations', {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            }).then(() => {
                lastUpdateTime = currentTime;
            });
        }
    }
    checkConsultationsUpdate();
    document.querySelectorAll('.bottom-box').forEach(box => {
        box.addEventListener('click', checkConsultationsUpdate);
    });
});