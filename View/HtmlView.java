package View;

public final class HtmlView {

    private HtmlView() {
    }

    public static String css() {
        return """
            :root {
                --bg: #eef4ff;
                --bg-soft: #f8fbff;
                --panel: rgba(255, 255, 255, 0.9);
                --panel-strong: #ffffff;
                --ink: #14213d;
                --muted: #52607a;
                --accent: #2563eb;
                --accent-dark: #1d4ed8;
                --accent-soft: #dbeafe;
                --border: #c8d7f2;
                --shadow: 0 20px 45px rgba(37, 99, 235, 0.12);
            }
            * { box-sizing: border-box; }
            body {
                margin: 0;
                font-family: Georgia, "Times New Roman", serif;
                background:
                    radial-gradient(circle at top right, rgba(37, 99, 235, 0.14), transparent 24%),
                    radial-gradient(circle at left center, rgba(14, 165, 233, 0.16), transparent 28%),
                    linear-gradient(180deg, #f8fbff 0%, var(--bg) 100%);
                color: var(--ink);
            }
            .shell {
                max-width: 1180px;
                margin: 0 auto;
                padding: 24px 18px 36px;
            }
            h1, h2, h3, p { margin-top: 0; }
            .hero {
                padding: 28px;
                border: 1px solid var(--border);
                border-radius: 28px;
                background:
                    linear-gradient(135deg, rgba(37, 99, 235, 0.1), rgba(248, 251, 255, 0.94)),
                    var(--panel-strong);
                box-shadow: var(--shadow);
                margin-bottom: 18px;
            }
            .eyebrow {
                display: inline-block;
                padding: 6px 10px;
                border-radius: 999px;
                background: var(--accent-soft);
                color: var(--accent-dark);
                font-size: 12px;
                letter-spacing: 0.08em;
                text-transform: uppercase;
                margin-bottom: 12px;
            }
            .hero h1 {
                font-size: clamp(32px, 5vw, 52px);
                line-height: 0.95;
                margin-bottom: 12px;
            }
            .hero p {
                max-width: 720px;
                color: var(--muted);
                font-size: 17px;
                line-height: 1.6;
            }
            nav {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
                margin: 0 0 18px;
            }
            nav a {
                text-decoration: none;
                padding: 10px 14px;
                border-radius: 999px;
                background: rgba(255, 251, 247, 0.85);
                background: rgba(255, 255, 255, 0.86);
                border: 1px solid var(--border);
                color: var(--ink);
                box-shadow: 0 8px 20px rgba(37, 99, 235, 0.08);
            }
            nav a:hover { background: var(--accent-soft); }
            .grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 16px;
            }
            .card {
                background: var(--panel);
                border: 1px solid var(--border);
                border-radius: 22px;
                padding: 18px;
                box-shadow: var(--shadow);
                backdrop-filter: blur(10px);
            }
            .card h2 {
                margin-bottom: 8px;
                font-size: 24px;
            }
            .lede {
                color: var(--muted);
                margin-bottom: 14px;
                line-height: 1.55;
            }
            .stats {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                gap: 12px;
                margin-top: 18px;
            }
            .stat {
                padding: 14px;
                border-radius: 18px;
                border: 1px solid var(--border);
                background: rgba(255, 255, 255, 0.62);
            }
            .stat strong {
                display: block;
                font-size: 28px;
                margin-bottom: 4px;
                color: var(--accent-dark);
            }
            .section-title {
                margin: 24px 0 12px;
                font-size: 26px;
            }
            label {
                display: block;
                margin: 10px 0 5px;
                font-weight: bold;
                color: var(--accent-dark);
            }
            input {
                width: 100%;
                border: 1px solid var(--border);
                border-radius: 12px;
                padding: 10px 12px;
                background: rgba(255, 255, 255, 0.9);
                color: var(--ink);
            }
            input::placeholder { color: #9c8374; }
            button {
                margin-top: 12px;
                border: 0;
                border-radius: 999px;
                background: var(--accent);
                color: white;
                padding: 10px 15px;
                cursor: pointer;
                font-weight: bold;
                letter-spacing: 0.02em;
            }
            button:hover { background: var(--accent-dark); }
            form + form {
                margin-top: 14px;
                padding-top: 14px;
                border-top: 1px solid var(--border);
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 12px;
                font-size: 14px;
            }
            th {
                color: var(--accent-dark);
                font-size: 12px;
                letter-spacing: 0.08em;
                text-transform: uppercase;
            }
            th, td {
                border-bottom: 1px solid var(--border);
                text-align: left;
                padding: 10px 8px;
            }
            .msg {
                border: 1px solid #edc98f;
                background: #fff2dd;
                border-radius: 16px;
                padding: 12px 14px;
                margin-bottom: 14px;
                box-shadow: 0 10px 25px rgba(37, 99, 235, 0.08);
            }
            .footer-note {
                margin-top: 20px;
                color: var(--muted);
                font-size: 14px;
                text-align: center;
            }
            @media (max-width: 700px) {
                .hero { padding: 22px; }
                .card { padding: 16px; }
                nav a { flex: 1 1 calc(50% - 10px); text-align: center; }
            }
            """;
    }

    public static String page(String title, String content) {
        return """
            <!doctype html>
            <html lang='pt-BR'>
            <head>
              <meta charset='utf-8'>
              <meta name='viewport' content='width=device-width,initial-scale=1'>
              <title>""" + title + """
              </title>
              <link rel='stylesheet' href='/styles.css'>
            </head>
            <body>
              <div class='shell'>
                """ + content + """
              </div>
            </body>
            </html>
            """;
    }

    public static String nav() {
        return """
            <nav>
              <a href='/'>Loja Online</a>
              <a href='/clientes'>Clientes</a>
              <a href='/produtos'>Catalogo</a>
              <a href='/cupons'>Cupons</a>
              <a href='/pedidos'>Pedidos</a>
            </nav>
            """;
    }

    public static String msgBox(String msg, String escaped) {
        if (msg == null || msg.isBlank()) {
            return "";
        }
        return "<div class='msg'>" + escaped + "</div>";
    }
}
