// Универсальный скрипт для поиска по таблице
function initTableSearch(searchInputId, clearBtnId, tableId) {
    const searchInput = document.getElementById(searchInputId);
    const table = document.getElementById(tableId);
    
    // Проверяем, что основные элементы существуют
    if (!searchInput || !table) {
        console.log('Search init failed: missing searchInput or table');
        return;
    }
    
    // Кнопка очистки может отсутствовать (если передали null)
    const clearBtn = clearBtnId ? document.getElementById(clearBtnId) : null;
    
    function filterTable() {
        const searchTerm = searchInput.value.toLowerCase().trim();
        const tbody = table.querySelector('tbody');
        if (!tbody) return;
        
        const rows = tbody.querySelectorAll('tr');
        let hasVisibleRows = false;
        
        rows.forEach(row => {
            // Пропускаем строку "нет данных" (с colspan)
            if (row.querySelector('td[colspan]')) {
                return;
            }
            
            const cells = row.querySelectorAll('td');
            let found = false;
            
            // Ищем по всем ячейкам, кроме первой (номер)
            for (let i = 1; i < cells.length; i++) {
                const cellText = cells[i].textContent.toLowerCase();
                if (cellText.indexOf(searchTerm) !== -1) {
                    found = true;
                    break;
                }
            }
            
            row.style.display = found ? '' : 'none';
            if (found) hasVisibleRows = true;
        });
        
        // Показываем/скрываем строку "нет данных"
        const noDataRow = tbody.querySelector('tr td[colspan]')?.parentElement;
        if (noDataRow) {
            noDataRow.style.display = hasVisibleRows ? 'none' : '';
        }
    }
    
    searchInput.addEventListener('input', filterTable);
    
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            searchInput.value = '';
            filterTable();
        });
    }
    
    // Выполняем фильтрацию при загрузке (на случай, если есть предустановленное значение)
    filterTable();
}

// Автоматическое скрытие alert-сообщений
function initAutoHideAlerts() {
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert-auto-hide');
        alerts.forEach(function(alert) {
            if (alert && alert.style) {
                alert.style.display = 'none';
            }
        });
    }, 5000);
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initAutoHideAlerts();
});