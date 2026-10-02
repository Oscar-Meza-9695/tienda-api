const $ = (s, r = document) => r.querySelector(s),
  $$ = (s, r = document) => [...r.querySelectorAll(s)];
const money = (n) =>
  new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" }).format(
    n ?? 0,
  );
const esc = (s) =>
  String(s ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const fdate = (d) =>
  d
    ? new Date(d).toLocaleString("es-MX", {
        dateStyle: "short",
        timeStyle: "short",
      })
    : "";
const ic = (n) => `<svg class="i"><use href="#i-${n}"/></svg>`;
const hue = (s) =>
  ([...String(s)].reduce((a, c) => a + c.charCodeAt(0), 0) * 37) % 360;
const hoy = () => new Date().toLocaleDateString("en-CA");
let tt;
function toast(m, bad) {
  const t = $("#toast");
  t.textContent = m;
  t.className = "on" + (bad ? " bad" : "");
  clearTimeout(tt);
  tt = setTimeout(() => (t.className = ""), 3500);
}
const csrf = () =>
  decodeURIComponent(
    (document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/) || [])[1] || "",
  );
async function api(path, opt = {}) {
  const r = await fetch("/api" + path, {
    headers: { "Content-Type": "application/json", "X-XSRF-TOKEN": csrf() },
    ...opt,
    body: opt.body ? JSON.stringify(opt.body) : undefined,
  });
  if (r.status === 401 && path !== "/auth/login") {
    mostrarLogin();
    throw new Error("Tu sesión terminó. Inicia sesión de nuevo.");
  }
  if (r.status === 403) throw new Error("Sesión no válida. Recarga la página.");
  if (r.status === 204) return null;
  let d = null;
  try {
    d = await r.json();
  } catch {}
  if (!r.ok) {
    const e = new Error(
      d?.error || Object.values(d || {}).join(". ") || "Error " + r.status,
    );
    e.status = r.status;
    e.data = d;
    throw e;
  }
  return d;
}
const safe =
  (fn) =>
  async (...a) => {
    try {
      await fn(...a);
    } catch (e) {
      toast(e.message, true);
    }
  };
const debounce = (f, ms = 300) => {
  let t;
  return (...a) => {
    clearTimeout(t);
    t = setTimeout(() => f(...a), ms);
  };
};

// ---- Modal ----
const dlg = $("#dlg");
function modal(html, onSubmit) {
  dlg.innerHTML = `<form method="dialog">${html}</form>`;
  dlg.showModal();
  const f = $("form", dlg);
  $$("[data-close]", dlg).forEach((b) => (b.onclick = () => dlg.close()));
  if (onSubmit)
    f.onsubmit = safe(async (e) => {
      e.preventDefault();
      await onSubmit(Object.fromEntries(new FormData(f)));
    });
}

// ---- Navegación ----
const loaders = {
  venta: cargarClientesSelect,
  productos: cargarProductos,
  clientes: cargarClientes,
  reportes: cargarReportes,
};
$$("nav button").forEach(
  (b) =>
    (b.onclick = () => {
      $$("nav button").forEach((x) => x.classList.toggle("on", x === b));
      $$("main>section").forEach((s) => (s.hidden = s.id !== b.dataset.v));
      safe(loaders[b.dataset.v])();
    }),
);

// ---- Venta ----
let carrito = [];
const total = () => carrito.reduce((t, i) => t + i.p.precio * i.qty, 0);
function agregar(p) {
  const x = carrito.find((i) => i.p.idProducto === p.idProducto);
  x ? (x.qty = +(x.qty + 1).toFixed(3)) : carrito.push({ p, qty: 1 });
  pintarCarrito();
}
function pintarCarrito() {
  $("#carrito").innerHTML = carrito.length
    ? carrito
        .map(
          (i, n) => `
    <div class="item"><span>${esc(i.p.nombre)}<br><small>${money(i.p.precio)}</small></span>
    <input type="number" min="0.001" step="0.001" value="${i.qty}" data-n="${n}" aria-label="Cantidad">
    <b class="n">${money(i.p.precio * i.qty)}</b><button class="x" data-x="${n}" aria-label="Quitar">${ic("x")}</button></div>`,
        )
        .join("")
    : '<p class="vacio">Agrega productos para empezar.</p>';
  $$("#carrito input").forEach(
    (e) =>
      (e.onchange = () => {
        const q = parseFloat(e.value);
        q > 0 ? (carrito[e.dataset.n].qty = q) : carrito.splice(e.dataset.n, 1);
        pintarCarrito();
      }),
  );
  $$("#carrito .x").forEach(
    (e) =>
      (e.onclick = () => {
        carrito.splice(e.dataset.x, 1);
        pintarCarrito();
      }),
  );
  $("#total").textContent = money(total());
  $("#cobrar").disabled = !carrito.length;
}
async function cargarClientesSelect() {
  const v = $("#cliente").value,
    cs = await api("/clientes");
  $("#cliente").innerHTML =
    '<option value="">Mostrador (sin cliente)</option>' +
    cs
      .map((c) => `<option value="${c.idCliente}">${esc(c.nombre)}</option>`)
      .join("");
  $("#cliente").value = v;
}
const tipo = () => $("input[name=tipo]:checked").value;
$$("input[name=tipo]").forEach(
  (r) =>
    (r.onchange = () => {
      $("#lmetodo").hidden = tipo() !== "CONTADO";
      $("#abonoBox").hidden = tipo() !== "CREDITO";
    }),
);
function mostrarResultados(ps) {
  $("#resultados").innerHTML = ps.length
    ? ps
        .map(
          (p) =>
            `<button class="prod" data-id="${p.idProducto}"><i class="av" style="--h:${hue(p.nombre)}">${esc(p.nombre[0])}</i><b>${esc(p.nombre)}</b><span>${money(p.precio)}</span></button>`,
        )
        .join("")
    : '<p class="vacio">Sin resultados.</p>';
  $$("#resultados .prod").forEach(
    (b) =>
      (b.onclick = () => agregar(ps.find((p) => p.idProducto == b.dataset.id))),
  );
}
$("#buscar").onkeydown = safe(async (e) => {
  if (e.key !== "Enter") return;
  const q = e.target.value.trim();
  if (!q) return;
  try {
    agregar(await api("/productos/codigo/" + encodeURIComponent(q)));
    e.target.value = "";
    $("#resultados").innerHTML = "";
  } catch (err) {
    if (err.status !== 404) throw err;
    mostrarResultados(
      await api("/productos/buscar?nombre=" + encodeURIComponent(q)),
    );
  }
});
$("#vaciar").onclick = () => {
  carrito = [];
  pintarCarrito();
};
$("#cobrar").onclick = safe(async () => {
  const body = {
    idCliente: $("#cliente").value || null,
    tipoPago: tipo(),
    items: carrito.map((i) => ({
      idProducto: i.p.idProducto,
      cantidad: i.qty,
    })),
  };
  if (tipo() === "CONTADO") body.metodoPago = $("#metodo").value;
  else if (+$("#abono").value > 0) {
    body.montoPagado = +$("#abono").value;
    body.metodoPagado = $("#metodoAbono").value;
  }
  const enviar = () => api("/ventas", { method: "POST", body });
  let v;
  try {
    v = await enviar();
  } catch (e) {
    if (
      e.status === 409 &&
      e.data?.codigo === "SALDO_A_FAVOR_REQUIERE_CONFIRMACION" &&
      confirm(e.message)
    ) {
      body.permitirSaldoAFavor = true;
      v = await enviar();
    } else throw e;
  }
  toast(`Venta #${v.idVenta} cobrada: ${money(v.total)}`);
  carrito = [];
  $("#abono").value = "";
  pintarCarrito();
  $("#buscar").focus();
});

// ---- Productos ----
let inact = false;
async function cargarProductos() {
  const q = $("#qProd").value.trim(),
    n = encodeURIComponent(q);
  const ps = await api(
    inact
      ? "/productos/inactivos" + (q ? "?nombre=" + n : "")
      : q
        ? "/productos/buscar?nombre=" + n
        : "/productos",
  );
  $("#tProd").innerHTML = ps.length
    ? ps
        .map(
          (
            p,
          ) => `<tr><td><div class="nom"><i class="av" style="--h:${hue(p.nombre)}">${esc(p.nombre[0])}</i><div>${esc(p.nombre)}<br><small>${esc(p.descripcion)}</small></div></div></td><td><span class="cod">${esc(p.codigoBarras)}</span></td><td class="n">${money(p.precio)}</td>
    <td class="acc n">${inact ? `<button class="sec ok" data-r="${p.idProducto}">${ic("undo")}Reactivar</button>` : `<button class="sec" data-e="${p.idProducto}">${ic("edit")}Editar</button><button class="sec peligro" data-d="${p.idProducto}">${ic("power")}Desactivar</button>`}</td></tr>`,
        )
        .join("")
    : `<tr><td colspan="4" class="vacio">${inact ? "No hay productos desactivados." : 'No hay productos. Crea el primero con "Nuevo producto".'}</td></tr>`;
  $$("#tProd [data-e]").forEach(
    (b) =>
      (b.onclick = () =>
        formProducto(ps.find((p) => p.idProducto == b.dataset.e))),
  );
  $$("#tProd [data-d]").forEach(
    (b) =>
      (b.onclick = safe(async () => {
        if (
          !confirm("¿Desactivar este producto? Dejará de aparecer para vender.")
        )
          return;
        await api("/productos/" + b.dataset.d, { method: "DELETE" });
        toast("Producto desactivado");
        cargarProductos();
      })),
  );
  $$("#tProd [data-r]").forEach(
    (b) =>
      (b.onclick = safe(async () => {
        await api("/productos/" + b.dataset.r + "/reactivar", {
          method: "PATCH",
        });
        toast("Producto reactivado: ya aparece para vender");
        cargarProductos();
      })),
  );
}
$$("#segProd button").forEach(
  (b) =>
    (b.onclick = () => {
      inact = b.dataset.i === "1";
      $$("#segProd button").forEach((x) => x.classList.toggle("on", x === b));
      $("#nuevoProd").hidden = inact;
      safe(cargarProductos)();
    }),
);
function formProducto(p = {}) {
  modal(
    `<h3>${p.idProducto ? "Editar" : "Nuevo"} producto</h3>
    <label>Nombre<input name="nombre" required value="${esc(p.nombre)}"></label>
    <label>Descripción<input name="descripcion" value="${esc(p.descripcion)}"></label>
    <label>Precio<input name="precio" type="number" min="0.01" step="0.01" required value="${p.precio ?? ""}"></label>
    <label>Código de barras<input name="codigoBarras" required value="${esc(p.codigoBarras)}"></label>
    <div class="btns"><button type="button" class="sec" data-close>Cancelar</button><button class="primario">Guardar</button></div>`,
    async (d) => {
      d.precio = +d.precio;
      await api(p.idProducto ? "/productos/" + p.idProducto : "/productos", {
        method: p.idProducto ? "PUT" : "POST",
        body: d,
      });
      dlg.close();
      toast("Producto guardado");
      cargarProductos();
    },
  );
}
$("#nuevoProd").onclick = () => formProducto();
$("#qProd").oninput = debounce(safe(cargarProductos));

// ---- Clientes ----
let inactC = false;
async function cargarClientes() {
  const q = $("#qCli").value.trim(),
    n = encodeURIComponent(q);
  const [cs, cxc] = await Promise.all([
    api(
      inactC
        ? "/clientes/inactivos" + (q ? "?nombre=" + n : "")
        : q
          ? "/clientes/buscar?nombre=" + n
          : "/clientes",
    ),
    api("/reportes/cuentas-por-cobrar"),
  ]);
  const debe = Object.fromEntries(
    cxc.deudores.map((d) => [d.idCliente, d.saldo]),
  );
  $("#tCli").innerHTML = cs.length
    ? cs
        .map(
          (
            c,
          ) => `<tr><td><div class="nom"><i class="av" style="--h:${hue(c.nombre)}">${esc(c.nombre[0])}</i>${esc(c.nombre)}</div></td><td class="n ${debe[c.idCliente] ? "debe" : ""}">${debe[c.idCliente] ? money(debe[c.idCliente]) : "—"}</td>
    <td class="acc n">${inactC ? `<button class="sec" data-s="${c.idCliente}">${ic("receipt")}Estado de cuenta</button><button class="sec ok" data-r="${c.idCliente}">${ic("undo")}Reactivar</button>` : `<button class="sec" data-s="${c.idCliente}">${ic("receipt")}Estado de cuenta</button><button class="sec" data-e="${c.idCliente}">${ic("edit")}Editar</button><button class="sec peligro" data-d="${c.idCliente}">${ic("power")}Desactivar</button>`}</td></tr>`,
        )
        .join("")
    : `<tr><td colspan="3" class="vacio">${inactC ? "No hay clientes desactivados." : 'No hay clientes. Agrega uno con "Nuevo cliente".'}</td></tr>`;
  $$("#tCli [data-s]").forEach(
    (b) => (b.onclick = safe(() => estadoCuenta(b.dataset.s))),
  );
  $$("#tCli [data-e]").forEach(
    (b) =>
      (b.onclick = () =>
        formCliente(cs.find((c) => c.idCliente == b.dataset.e))),
  );
  $$("#tCli [data-d]").forEach(
    (b) =>
      (b.onclick = safe(async () => {
        if (!confirm("¿Desactivar este cliente?")) return;
        await api("/clientes/" + b.dataset.d, { method: "DELETE" });
        toast("Cliente desactivado");
        cargarClientes();
      })),
  );
  $$("#tCli [data-r]").forEach(
    (b) =>
      (b.onclick = safe(async () => {
        await api("/clientes/" + b.dataset.r + "/reactivar", {
          method: "PATCH",
        });
        toast("Cliente reactivado: ya puede comprar y fiar");
        cargarClientes();
      })),
  );
}
$$("#segCli button").forEach(
  (b) =>
    (b.onclick = () => {
      inactC = b.dataset.i === "1";
      $$("#segCli button").forEach((x) => x.classList.toggle("on", x === b));
      $("#nuevoCli").hidden = inactC;
      safe(cargarClientes)();
    }),
);
function formCliente(c = {}) {
  modal(
    `<h3>${c.idCliente ? "Editar" : "Nuevo"} cliente</h3><label>Nombre<input name="nombre" required value="${esc(c.nombre)}"></label>
    <div class="btns"><button type="button" class="sec" data-close>Cancelar</button><button class="primario">Guardar</button></div>`,
    async (d) => {
      await api(c.idCliente ? "/clientes/" + c.idCliente : "/clientes", {
        method: c.idCliente ? "PUT" : "POST",
        body: d,
      });
      dlg.close();
      toast("Cliente guardado");
      cargarClientes();
    },
  );
}
$("#nuevoCli").onclick = () => formCliente();
$("#qCli").oninput = debounce(safe(cargarClientes));
async function estadoCuenta(id) {
  const e = await api(`/clientes/${id}/estado-cuenta`),
    s = e.saldo;
  const txt =
    s.situacion === "DEBE"
      ? `<b class="debe">Debe ${money(s.saldo)}</b>`
      : s.situacion === "A_FAVOR"
        ? `<b class="favor">A favor ${money(-s.saldo)}</b>`
        : "<b>Al corriente</b>";
  modal(
    `<h3>${esc(s.nombre)}</h3><p>${txt}</p>
    <table><tbody>${
      e.movimientos
        .map(
          (
            m,
          ) => `<tr style="${m.anulada ? "text-decoration:line-through;opacity:.5" : ""}"><td>${fdate(m.fecha)}<br>${m.productos?.length ? `<details class="det"><summary>${esc(m.detalle)} · ${m.productos.length} producto${m.productos.length > 1 ? "s" : ""}</summary><ul>${m.productos.map((x) => `<li><span>${x.cantidad} × ${esc(x.nombreProducto)} <small>(${money(x.precioUnitario)})</small></span><b>${money(x.subtotal)}</b></li>`).join("")}</ul></details>` : `<small>${esc(m.detalle)}</small>`}</td>
      <td class="n ${m.tipo === "PAGO" ? "favor" : "debe"}">${m.tipo === "PAGO" ? "−" : "+"}${money(m.monto)}</td></tr>`,
        )
        .join("") || '<tr><td class="vacio">Sin movimientos todavía.</td></tr>'
    }</tbody></table>
    <h3 style="margin-top:1rem">Registrar abono</h3>
    <label>Monto<input name="monto" type="number" min="0.01" step="0.01" required></label>
    <label>Método<select name="metodoPago"><option value="EFECTIVO">Efectivo</option><option value="TRANSFERENCIA">Transferencia</option></select></label>
    <div class="btns"><button type="button" class="sec" data-close>Cerrar</button><button class="primario">Registrar abono</button></div>`,
    async (d) => {
      const body = { monto: +d.monto, metodoPago: d.metodoPago };
      const enviar = () =>
        api(`/clientes/${id}/pagos`, { method: "POST", body });
      try {
        await enviar();
      } catch (err) {
        if (
          err.status === 409 &&
          err.data?.codigo === "SALDO_A_FAVOR_REQUIERE_CONFIRMACION" &&
          confirm(err.message)
        ) {
          body.permitirSaldoAFavor = true;
          await enviar();
        } else throw err;
      }
      toast("Abono registrado");
      await estadoCuenta(id);
      cargarClientes();
    },
  );
}

// ---- Reportes ----
$("#desde").value = $("#hasta").value = hoy();
async function cargarReportes() {
  const d = $("#desde").value,
    h = $("#hasta").value,
    qs = `?desde=${d}&hasta=${h}`;
  const [c, top, cxc] = await Promise.all([
    api("/reportes/corte-caja" + qs),
    api("/reportes/productos-top" + qs),
    api("/reportes/cuentas-por-cobrar"),
  ]);
  const card = (t, v, x = "") =>
    `<div class="card"><small>${t}</small><strong ${x}>${v}</strong></div>`;
  $("#rCorte").innerHTML =
    card("Total en caja", money(c.totalCaja)) +
    Object.entries(c.cajaPorMetodo || {})
      .map(([m, v]) =>
        card(m === "EFECTIVO" ? "Efectivo" : "Transferencia", money(v)),
      )
      .join("") +
    card("Ventas de contado", money(c.ventasContado)) +
    card("Abonos recibidos", money(c.abonosRecibidos)) +
    card("Vendido a crédito (no es dinero)", money(c.ventasACuenta)) +
    card("Número de ventas", c.numeroVentas);
  $("#rTop").innerHTML =
    top
      .map(
        (p) =>
          `<tr><td>${esc(p.producto)}</td><td class="n">${p.cantidadVendida}</td><td class="n">${money(p.totalIngresos)}</td></tr>`,
      )
      .join("") ||
    '<tr><td colspan="3" class="vacio">Sin ventas en este periodo.</td></tr>';
  $("#rCxc").innerHTML =
    `<div class="cards">${card("Por cobrar", money(cxc.totalPorCobrar), 'class="debe"')}${card("Saldo a favor de clientes", money(cxc.totalSaldoAFavor))}</div>
    <table style="margin-top:.7rem"><tbody>${cxc.deudores.map((x) => `<tr><td>${esc(x.nombre)}</td><td class="n debe">${money(x.saldo)}</td></tr>`).join("") || '<tr><td class="vacio">Nadie debe nada.</td></tr>'}</tbody></table>`;
}
$("#verReporte").onclick = safe(cargarReportes);

// ---- Sesión ----
function mostrarLogin() {
  document.body.classList.remove("cargando", "auth");
  dlg.open && dlg.close();
  carrito = [];
  pintarCarrito();
}
function iniciar(u) {
  document.body.classList.remove("cargando");
  document.body.classList.add("auth");
  $("#usuario").textContent = "Sesión: " + u.username;
  safe(cargarClientesSelect)();
  $("#buscar").focus();
}
$("#loginForm").onsubmit = async (e) => {
  e.preventDefault();
  $("#loginError").textContent = "";
  try {
    const u = await api("/auth/login", {
      method: "POST",
      body: Object.fromEntries(new FormData(e.target)),
    });
    e.target.reset();
    iniciar(u);
  } catch (err) {
    $("#loginError").textContent = err.message;
  }
};
$("#salir").onclick = safe(async () => {
  await api("/auth/logout", { method: "POST" });
  mostrarLogin();
});
$("#cambiarPass").onclick = () =>
  modal(
    `<h3>Cambiar contraseña</h3>
  <label>Contraseña actual<input name="passwordActual" type="password" autocomplete="current-password" required></label>
  <label>Contraseña nueva (mínimo 8 caracteres)<input name="newPassword" type="password" minlength="8" autocomplete="new-password" required></label>
  <label>Repite la nueva<input name="repite" type="password" minlength="8" autocomplete="new-password" required></label>
  <div class="btns"><button type="button" class="sec" data-close>Cancelar</button><button class="primario">Cambiar</button></div>`,
    async (d) => {
      if (d.newPassword !== d.repite)
        throw new Error("Las contraseñas nuevas no coinciden");
      await api("/auth/cambiar-password", {
        method: "POST",
        body: { passwordActual: d.passwordActual, newPassword: d.newPassword },
      });
      dlg.close();
      toast("Contraseña actualizada");
    },
  );
pintarCarrito();
api("/auth/me")
  .then(iniciar)
  .catch(() => {});
