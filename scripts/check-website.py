"""Browser regressions using local files and fixtures; never sends usage reports."""
import re
from pathlib import Path
from urllib.parse import urlparse
from playwright.sync_api import sync_playwright, expect

ROOT = Path(__file__).resolve().parents[1] / 'site'
OUT = Path('website-reports')
OUT.mkdir(exist_ok=True)

def route(request):
    url = urlparse(request.request.url)
    if url.netloc == 'api.github.com':
        assert 'authorization' not in request.request.headers, 'Owner session must never reach GitHub'
        request.fulfill(json=[{'tag_name': 'v0.4.' + str(i), 'assets': [{'name': 'app.apk', 'download_count': 10}]} for i in range(12)] if '/releases' in url.path else {})
    elif url.netloc == 'opentune.test':
        path = ROOT / url.path.lstrip('/')
        if path.is_dir(): path = path / 'index.html'
        if path.is_file():
            mime = {'.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.css': 'text/css', '.json': 'application/json', '.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml', '.ico': 'image/x-icon', '.webmanifest': 'application/manifest+json'}.get(path.suffix, 'text/plain')
            request.fulfill(body=path.read_bytes(), content_type=mime)
        else: request.fulfill(status=404, body='')
    else:
        request.abort()

def no_overflow(page):
    sizes = page.evaluate('({width:innerWidth, client:document.documentElement.clientWidth, scroll:document.documentElement.scrollWidth, body:document.body.scrollWidth, scale:visualViewport.scale})')
    assert sizes['scroll'] <= sizes['client'] + 1, sizes
    assert abs(sizes['scale'] - 1) < .02, sizes

with sync_playwright() as p:
    browser = p.chromium.launch()
    for width in (320, 360, 390, 768, 1024, 1440):
        context = browser.new_context(viewport={'width': width, 'height': 844}, is_mobile=width < 768, has_touch=width < 768, reduced_motion='reduce')
        context.route('**/*', route)
        page = context.new_page()
        errors = []
        page.on('pageerror', lambda error: errors.append(str(error)))
        page.goto('http://opentune.test/')
        page.wait_for_timeout(300)
        no_overflow(page)
        page.reload()
        page.wait_for_timeout(300)
        no_overflow(page)
        page.evaluate('window.scrollTo(0, 1200)')
        page.wait_for_timeout(100)
        header = page.locator('.nav').bounding_box()
        assert header['y'] >= -1 and header['x'] >= -1, header
        for element in page.locator('.nav a:visible').all():
            box = element.bounding_box()
            assert box['x'] >= -1 and box['x'] + box['width'] <= width + 1, box
            assert box['y'] >= 0 and box['y'] + box['height'] <= header['height'] + 1, box
        assert page.locator('meta[property="og:image"]').get_attribute('content').endswith('/share-card-v3.jpg')
        page.screenshot(path=str(OUT / f'home-{width}.png'))
        page.goto('http://opentune.test/privacy/')
        no_overflow(page)
        page.screenshot(path=str(OUT / f'privacy-{width}.png'))
        assert not errors, errors
        context.close()
    browser.close()
print('Passed mobile refresh/header, privacy and wrapping checks.')
