package org.psjava.site;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.psjava.util.DataKeeper;

import play.api.Play;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class SolutionSiteController extends Controller {

	public static Promise<Result> index() {
		final File f = Play.getFile("public/solutions-master/LICENSE", Play.current());
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				String content = FileUtil.loadUTF8(f);
				return ok(content);
			}
		});
	}

	public static Promise<Result> showSolution(final String siteName, final String problemId, final String title) {
		if (containsUpper(siteName) || containsUpper(title))
			return createNotFoundPromise();
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				final File zipFile = Play.getFile("public/solutions-master.zip", Play.current());
				String content;

				ZipFile z = new ZipFile(zipFile);
				try {
					ZipEntry e = z.getEntry("solutions-master/src/main/java/org/psjava/solutions/code/" + siteName + "_" + problemId.toLowerCase() + "/" + "Main.java");
					if (e == null)
						return notFound("unknown problem");
					InputStream is = z.getInputStream(e);
					try {
						content = FileUtil.loadUTF8(is).trim();
					} finally {
						is.close();
					}
				} finally {
					z.close();
				}
				CompilationUnit cu = JavaParser.parse(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
				final DataKeeper<Boolean> ok = DataKeeper.create(false);
				final ArrayList<String> hints = new ArrayList<String>();
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(ClassOrInterfaceDeclaration n, Object arg) {
						JavadocComment doc = n.getJavaDoc();
						Scanner scan = new Scanner(doc.getContent());
						while (scan.hasNextLine()) {
							String line = scan.nextLine();
							if (line.contains("@title")) {
								String titleInDoc = line.substring(line.indexOf("@title") + 6).trim();
								String adjusted = titleInDoc.replace(' ', '-').toLowerCase();
								if (adjusted.equals(title))
									ok.set(true);
							}
							if (line.contains("@hint"))
								hints.add(line.substring(line.indexOf("@hint") + 5).trim());
						}
						scan.close();
					}
				}.visit(cu, null);
				if (ok.get())
					return ok(solution.render(content, hints, siteName, problemId));
				else
					return notFound("unknown problem");
			}
		});
	}

	private static boolean containsUpper(String text) {
		return !text.toLowerCase().equals(text);
	}

	private static Promise<Result> createNotFoundPromise() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return badRequest("bad request");
			}
		});
	}
}
