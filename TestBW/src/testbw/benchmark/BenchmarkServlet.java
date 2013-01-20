package testbw.benchmark;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import testbw.server.GetKnappsterSiegerServiceImpl;
import testbw.server.GetMandateServiceImpl;
import testbw.server.GetMembersServiceImpl;
import testbw.server.SeatDistributionServiceImpl;
import testbw.server.WahlkreisOverviewServiceImpl;
import testbw.server.WahlkreissiegerServiceImpl;

public class BenchmarkServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		PrintWriter out = res.getWriter();

		final String queryNumber = req.getParameter("query");
		final String jahr = req.getParameter("jahr");
		final String wahlkreis = req.getParameter("wahlkreis");

		String[] queryInput = { jahr, wahlkreis };
		String[] projectInput = { "Bundestagswahl", "user", "1234" };

		switch (queryNumber) {
		case "1":
			SeatDistributionServiceImpl querySeatDistribution = new SeatDistributionServiceImpl();
			querySeatDistribution.getSeatDistribution(projectInput, queryInput);

			break;
		case "2":
			GetMembersServiceImpl queryMembers = new GetMembersServiceImpl();

			queryMembers.getMembers(projectInput, queryInput);
			break;
		case "3":
			WahlkreisOverviewServiceImpl queryWahlkreis = new WahlkreisOverviewServiceImpl();

			queryWahlkreis.getWKOverview(projectInput, queryInput);
			break;
		case "4":
			WahlkreissiegerServiceImpl queryWahlkreissieger = new WahlkreissiegerServiceImpl();

			queryWahlkreissieger.getWahlkreissieger(projectInput, queryInput);
			break;
		case "5":
			GetMandateServiceImpl queryUeberhangsmandate = new GetMandateServiceImpl();

			queryUeberhangsmandate.getMandate(projectInput, queryInput);
			break;
		case "6":
			GetKnappsterSiegerServiceImpl queryKnappsterSieger = new GetKnappsterSiegerServiceImpl();

			queryKnappsterSieger.getKnappsterSieger(projectInput, queryInput);
			break;
		default:
			break;
		}

		out.println("Query verarbeitet");
		out.close();
	}
}