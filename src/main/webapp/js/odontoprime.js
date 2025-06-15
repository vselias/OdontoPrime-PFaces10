window.addEventListener('load', function() {

	function fecharSubmenus() {
		document.querySelectorAll(".ui-menu-list ul").forEach(sub => {
			sub.classList.remove("show-submenu");
		});
	}

	function fecharSubMenusByClick() {
		document.addEventListener("click", (event) => {
			let menuItems = document.querySelectorAll(".ui-menu-list > li");
			var menu = document.querySelector(".ui-menu-list");
			let clicouDentro = false;
			var btFechar = document.querySelector("#btFecharMenu");



			// Verifica se o clique ocorreu dentro de algum item do menu
			menuItems.forEach(item => {
				if (item.contains(event.target) || menu.contains(event.target)) {
					clicouDentro = true;
				}
			});

			if (menu.contains(event.target)) {
				fecharSubmenus();
			}
			// Se NÃO clicou dentro de um item do menu, fecha o menu
			if (!clicouDentro) {
				menu.classList.remove("showMenu");
				if (btFechar)
					btFechar.remove();
				console.log("Menu fechado");
			}

			// Verifica se o clique aconteceu dentro de uma LI, mas FORA do A
			menuItems.forEach(li => {
				let link = li.querySelector("a");

				if (li.contains(event.target) && event.target !== link) {

				}
			});
		});
	}

	function showSubMenu() {
		document.querySelectorAll(".ui-menu-list > li > a").forEach(link => {
			link.addEventListener("click", function(event) {
				let submenu = this.nextElementSibling; // Captura o UL logo após o A

				if (submenu && submenu.tagName === "UL") {
					event.preventDefault(); // Impede a navegação imediata
					// **Impede que o evento global feche o submenu imediatamente**
					event.stopPropagation();
					fecharSubmenus(); // Fecha outros submenus antes de abrir um novo
					submenu.classList.add("show-submenu"); // Abre o submenu corretamente
				}
			});
		});
	}

	function posicionarMenuScroll() {
		const divMenu = document.querySelector(".ui-menu.ui-menubar");

		// Verifica se o menu foi encontrado
		if (!divMenu) {
			console.error("Elemento do menu não encontrado!");
			return;
		}
		document.addEventListener("scroll", () => {

			if (window.scrollY > 0) {
				divMenu.style.position = "fixed !important";
				divMenu.style.top = "125px !important";
				divMenu.style.width = "100% !important";
				divMenu.style.maxWidth = "100vw !important"; // Ajuste conforme necessário
			} else {
				divMenu.style.position = "absolute !important"; // Ou "absolute" dependendo do layout
				divMenu.style.top = "auto !important";
			}
		});

	}

	function criarBtFechar() {
		var selectBtFechar = document.querySelector("#btFecharMenu");
		var divMenu = document.querySelector(".ui-menu.ui-menubar");

		console.log("Div menu:");
		console.log(divMenu);
		if (selectBtFechar)
			selectBtFechar.remove();
		let botaoFechar = document.createElement("span");
		botaoFechar.textContent = "X";
		botaoFechar.id = "btFecharMenu";
		divMenu.appendChild(botaoFechar);
	}
	function showMenu() {
		var btnMenu = document.querySelector(".btnMenu");
		btnMenu.addEventListener("click", (event) => {
			event.preventDefault();
			event.stopPropagation();
			var menu = document.querySelector(".ui-menu-list");
			criarBtFechar();
			menu.classList.toggle("showMenu");
			setTimeout(() => {
				console.log("Classe ativa? ", menu.classList.contains("showMenu"));
			}, 1000);
		});
	}
	posicionarMenuScroll();
	showMenu();
	showSubMenu();
	fecharSubMenusByClick();

});


